package eu.stamp_project.dspot.assertiongenerator.assertiongenerator;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.Observation;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationException;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.AssertionSyntaxBuilder;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.Observer;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;

import eu.stamp_project.dspot.common.compilation.TestCompiler;
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

import java.util.*;
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

    private final boolean devFriendlyAmplification;

    public MethodReconstructor(double delta,
                               CtType originalClass,
                               DSpotCompiler compiler,
                               Map<CtMethod<?>, List<CtLocalVariable<?>>> variableReadsAsserted,
                               TestCompiler testCompiler,
                               boolean devFriendlyAmplification) {
        this.delta = delta;
        this.factory = compiler.getFactory();
        this.observer = new Observer(
                originalClass,
                compiler,
                variableReadsAsserted,
                testCompiler
        );
        this.devFriendlyAmplification = devFriendlyAmplification;
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
        if (devFriendlyAmplification) {
            testCases.addAll(AmplificationHelper.getFirstParentsIfExist(testCases));
        }

        try {
            observations = observer.getObservations(testClass, testCases);
        } catch (AmplificationException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return buildEachTest(testCases, observations);
    }

    // add assertions to each test with values retrieved from logs
    private List<CtMethod<?>> buildEachTest(List<CtMethod<?>> testCases, Map<String, Observation> observations) {
        LOGGER.info("Generating assertions...");
        if (devFriendlyAmplification) {
            return testCases.stream()
                    .map(ctMethod -> this.buildTestsWithSeparateAsserts(ctMethod, observations))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } else {
            return testCases.stream()
                    .map(ctMethod -> this.buildTestWithAssert(ctMethod, observations))
                    .collect(Collectors.toList());
        }
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
        CtMethod testWithAssert = CloneHelper.cloneTestMethodForAmp(test, "_ass");
        Integer numberOfAddedAssertion = 0;
        List<CtStatement> statements = Query.getElements(testWithAssert, new TypeFilter(CtStatement.class));

        // for every observation, create an assertion
        for (String id : observations.keySet()) {
            if (!id.split("__")[0].equals(test.getSimpleName())) {
                continue;
            }
            final List<CtStatement> assertStatements = AssertionSyntaxBuilder.buildAssert(
                    testWithAssert,
                    observations.get(id).getNotDeterministValues(),
                    observations.get(id).getObservationValues(),
                    this.delta
            );

            /* skip the current observation if it leads to
            an assertion identical to the last assertion put into the test method */
            if (assertStatements.stream()
                    .map(Object::toString)
                    .map("// AssertionGenerator: add assertion\n"::concat)
                    .anyMatch(testWithAssert.getBody().getLastStatement().toString()::equals)) {
                continue;
            }
            numberOfAddedAssertion = goThroughAssertionStatements(assertStatements,id,statements,numberOfAddedAssertion);
        }
        Counter.updateAssertionOf(testWithAssert, numberOfAddedAssertion);
        return decideReturn(testWithAssert,test);
    }

    /**
     * Adds new assertions one by one to generate several new test cases.
     *
     * @param test Original test method
     * @param observations Observation points of the test suite
     * @return A list of tests, each with a different new assertion of an observation point
     */
    private List<CtMethod<?>> buildTestsWithSeparateAsserts(CtMethod<?> test, Map<String, Observation> observations) {
        List<CtMethod<?>> testsToReturn = new ArrayList<>();
        CtMethod<?> clonedNoAmpTest = CloneHelper.cloneTestMethodNoAmp(test);

        // for every observation, create a new test with a matching assertion
        for (String id : observations.keySet()) {
            if (!id.split("__")[0].equals(test.getSimpleName())) {
                continue;
            }
            // if there is a '__end' observation for the same method, skip this one and use that one
            // (assertions should be at the end of the test case where possible)
            if (observations.containsKey(id + "___end")) {
                continue;
            }

            // check whether the observation exists in parent test case
            CtMethod<?> parent = AmplificationHelper.getAmpTestParent(test);
            List<CtStatement> parentAssertStatements = Collections.emptyList();
            if (parent != null) {
                String parentKey = parent.getSimpleName() + "__" + id.split("__", 2)[1];
                if (observations.containsKey(parentKey)) {
                    parentAssertStatements = AssertionSyntaxBuilder.buildAssert(parent,
                            observations.get(parentKey).getNotDeterministValues(),
                            observations.get(parentKey).getObservationValues(),this.delta);
                }
            }

            final List<CtStatement> assertStatements = AssertionSyntaxBuilder.buildAssert(
                    test,
                    observations.get(id).getNotDeterministValues(),
                    observations.get(id).getObservationValues(),
                    this.delta
            );

            for (CtStatement statement : assertStatements) {
                // skip if same statement could also appear in parent test
                if (!parentAssertStatements.isEmpty() && parentAssertStatements.contains(statement)) {
                    continue;
                }

                CtMethod<?> testWithAssert = CloneHelper.cloneTestMethodForAmp(clonedNoAmpTest, "_assSep");
                List<CtStatement> statements = Query.getElements(testWithAssert, new TypeFilter<CtStatement>(CtStatement.class));

                int numberOfAddedAssertion = goThroughAssertionStatements(Collections.singletonList(statement), id, statements, 0);
                Counter.updateAssertionOf(testWithAssert, numberOfAddedAssertion);
                testsToReturn.add(decideReturn(testWithAssert,test));
            }
        }
        return testsToReturn;
    }

    private int goThroughAssertionStatements(List<CtStatement> assertStatements,String id,
                                               List<CtStatement> statements, int numberOfAddedAssertion){
        int line = Integer.parseInt(id.split("__")[1]);
        CtStatement lastStmt = null;
        for (CtStatement assertStatement : assertStatements) {
            DSpotUtils.addComment(assertStatement,
                    "AssertionGenerator: add assertion",
                    CtComment.CommentType.INLINE,
                    CommentEnum.Amplifier);
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
        return numberOfAddedAssertion;
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
        // move comments from invocation to new variable statement
        List<CtComment> invocationComments = new ArrayList<>(statementToBeAsserted.getComments());
        invocationComments.forEach(comment -> {
            invocationToBeReplaced.removeComment(comment);
            localVariable.addComment(comment);
        });

        DSpotUtils.addComment(localVariable,
                "AssertionGenerator: create local variable with return value of invocation",
                CtComment.CommentType.INLINE,
                CommentEnum.Amplifier);
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
