package eu.stamp_project.dspot.assertiongenerator.assertiongenerator;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.Observation;
import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.AssertionSyntaxBuilder;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.Observer;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;

import eu.stamp_project.utils.compilation.TestCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class MethodReconstructor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodReconstructor.class);

    private Factory factory;

    private Observer observer;

    private double delta;

    public MethodReconstructor(double delta,
                               CtType originalClass,
                               DSpotCompiler compiler,
                               Map<CtMethod<?>, List<CtLocalVariable<?>>> variableReadsAsserted,
                               TestCompiler testCompiler) {
        this.delta = delta;
        this.factory = compiler.getFactory();
        this.observer = new Observer(
                originalClass,
                compiler,
                variableReadsAsserted,
                testCompiler
        );
    }

    /**
     * Adds new assertions in multiple passing tests.
     *
     * Instruments the tests to have observation points.
     * Details in {@link Observer#getObservations(CtType, List)}.
     *
     * Details of the assertion generation in {@link #buildTestWithAssert(CtMethod, Map)}.
     *
     * @param testClass Test class
     * @param testCases Passing test methods
     * @return New tests with new assertions generated from observation points values
     */
    public List<CtMethod<?>> addAssertions(CtType<?> testClass, List<CtMethod<?>> testCases) {
        Map<String, Observation> observations;
        try {
            observations = observer.getObservations(testClass, testCases);
        } catch (AmplificationException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return buildEachTest(testCases,observations);
    }

    // add assertions to each test with values retrieved from logs
    private List<CtMethod<?>> buildEachTest(List<CtMethod<?>> testCases,Map<String, Observation> observations) {
        LOGGER.info("Generating assertions...");
        return testCases.stream()
                .map(ctMethod -> this.buildTestWithAssert(ctMethod, observations))
                .collect(Collectors.toList());
    }

    /**
     * Adds new assertions to a test from observation points.
     *
     * Details of constructing the syntax for the assertions in {@link AssertionSyntaxBuilder#buildAssert(CtMethod, Set, Map, Double)}.
     * @param test         Test method
     * @param observations Observation points of the test suite
     * @return Test with new assertions
     */
    @SuppressWarnings("unchecked")
    private CtMethod<?> buildTestWithAssert(CtMethod test, Map<String, Observation> observations) {
        CtMethod testWithAssert = CloneHelper.cloneTestMethodForAmp(test, "");
        Integer numberOfAddedAssertion = 0;
        List<CtStatement> statements = Query.getElements(testWithAssert, new TypeFilter(CtStatement.class));

        // for every observation, create an assertion
        for (String id : observations.keySet()) {
            if (!id.split("__")[0].equals(testWithAssert.getSimpleName())) {
                continue;
            }
            final List<CtStatement> assertStatements = AssertionSyntaxBuilder.buildAssert(
                    test,
                    observations.get(id).getNotDeterministValues(),
                    observations.get(id).getObservationValues(),
                    this.delta
            );

            /* skip the current observation if it leads to
            an assertion identical to the last assertion put into the test method */
            if (assertStatements.stream()
                    .map(Object::toString)
                    .map("// AssertionGenerator add assertion\n"::concat)
                    .anyMatch(testWithAssert.getBody().getLastStatement().toString()::equals)) {
                continue;
            }
            goThroughAssertionStatements(assertStatements,id,statements,numberOfAddedAssertion);
        }
        Counter.updateAssertionOf(testWithAssert, numberOfAddedAssertion);
        return decideReturn(testWithAssert,test);
    }

    private void goThroughAssertionStatements(List<CtStatement> assertStatements,String id,
                                               List<CtStatement> statements,Integer numberOfAddedAssertion){
        int line = Integer.parseInt(id.split("__")[1]);
        CtStatement lastStmt = null;
        for (CtStatement assertStatement : assertStatements) {
            DSpotUtils.addComment(assertStatement,
                    "AssertionGenerator add assertion",
                    CtComment.CommentType.INLINE);
            try {
                CtStatement statementToBeAsserted = statements.get(line);
                if (lastStmt == null) {
                    lastStmt = statementToBeAsserted;
                }
                if (statementToBeAsserted instanceof CtBlock) {
                    break;
                }
                decideInvocationReplacement(statementToBeAsserted,id,assertStatement,statements,line,lastStmt);
                lastStmt = assertStatement;
                numberOfAddedAssertion++;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void decideInvocationReplacement(CtStatement statementToBeAsserted,String id,CtStatement assertStatement,
                                   List<CtStatement> statements,int line,CtStatement lastStmt){

        /* if the statement to be asserted is a method or constructor call, replace that invocation in the
        assertion with an equivalent local variable */
        if (statementToBeAsserted instanceof CtInvocation &&
                !AssertionGeneratorUtils.isVoidReturn((CtInvocation) statementToBeAsserted) &&
                statementToBeAsserted.getParent() instanceof CtBlock) {
            replaceInvocation(statementToBeAsserted,id,assertStatement,statements,line);

            // no creation of local variable is needed, just put the assertion into the test method
        } else {
            addAtCorrectPlace(id, lastStmt, assertStatement, statementToBeAsserted);
        }
    }

    private void replaceInvocation(CtStatement statementToBeAsserted,String id,CtStatement assertStatement,
                                   List<CtStatement> statements,int line){

        // create a new local variable and assign the invocation to it
        CtInvocation invocationToBeReplaced = (CtInvocation) statementToBeAsserted.clone();
        final CtLocalVariable localVariable = factory.createLocalVariable(
                AssertionGeneratorUtils.getCorrectTypeOfInvocation(invocationToBeReplaced),
                "o_" + id.split("___")[0],
                invocationToBeReplaced
        );

        // put the new local variable into the assertion and the assertion into the test method
        statementToBeAsserted.replace(localVariable);
        DSpotUtils.addComment(localVariable,
                "AssertionGenerator create local variable with return value of invocation",
                CtComment.CommentType.INLINE);
        localVariable.setParent(statementToBeAsserted.getParent());
        addAtCorrectPlace(id, localVariable, assertStatement, statementToBeAsserted);
        statements.remove(line);
        statements.add(line, localVariable);
    }

    private void addAtCorrectPlace(String id,
                                   CtStatement lastStmt,
                                   CtStatement assertStatement,
                                   CtStatement statementToBeAsserted) {
        if (id.endsWith("end")) {
            statementToBeAsserted.getParent(CtBlock.class).insertEnd(assertStatement);
        } else {
            lastStmt.insertAfter(assertStatement);
        }
    }

    private CtMethod decideReturn(CtMethod testWithAssert,CtMethod test){
        if (!testWithAssert.equals(test)) {
            return testWithAssert;
        } else {
            AmplificationHelper.removeAmpTestParent(testWithAssert);
            return null;
        }
    }
}
