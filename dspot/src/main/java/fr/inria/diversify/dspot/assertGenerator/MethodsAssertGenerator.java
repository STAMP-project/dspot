package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.compare.Observation;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.dspot.support.TestCompiler;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.TestTimedOutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class MethodsAssertGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodsAssertGenerator.class);

    private int numberOfFail = 0;

    private CtType originalClass;

    private Factory factory;

    private InputConfiguration configuration;

    private DSpotCompiler compiler;

    public MethodsAssertGenerator(CtType originalClass, InputConfiguration configuration, DSpotCompiler compiler) {
        this.originalClass = originalClass;
        this.configuration = configuration;
        this.compiler = compiler;
        this.factory = configuration.getInputProgram().getFactory();
    }

    public List<CtMethod<?>> generateAsserts(CtType testClass, List<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        LOGGER.info("Run tests. ({})", tests.size());
        final TestListener result = TestCompiler.compileAndRun(testClass,
                this.compiler,
                tests,
                this.configuration
        );
        if (result == null) {
            return Collections.emptyList();
        } else {
            final List<String> failuresMethodName = result.getFailingTests()
                    .stream()
                    .map(Failure::getDescription)
                    .map(Description::getMethodName)
                    .collect(Collectors.toList());
            final List<String> passingMethodName = result.getPassingTests()
                    .stream()
                    .map(Description::getMethodName)
                    .collect(Collectors.toList());

            final List<CtMethod<?>> generatedTestWithAssertion = new ArrayList<>();
            // add assertion on passing tests
            if (!passingMethodName.isEmpty()) {
                LOGGER.info("{} test pass, generating assertion...", passingMethodName.size());
                List<CtMethod<?>> passingTests = addAssertions(testClass,
                        tests.stream()
                                .filter(ctMethod -> passingMethodName.contains(ctMethod.getSimpleName()))
                                .collect(Collectors.toList()))
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (passingTests != null) {
                    generatedTestWithAssertion.addAll(passingTests);
                }
            }

            // add try/catch/fail on failing/error tests
            if (!failuresMethodName.isEmpty()) {
                LOGGER.info("{} test fail, generating try/catch/fail blocks...", failuresMethodName.size());
                final List<CtMethod<?>> failingTests = tests.stream()
                        .filter(ctMethod ->
                                failuresMethodName.contains(ctMethod.getSimpleName()))
                        .map(ctMethod ->
                                makeFailureTest(ctMethod, result.getFailureOf(ctMethod.getSimpleName()))
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!failingTests.isEmpty()) {
                    generatedTestWithAssertion.addAll(failingTests);
                }
            }
            return generatedTestWithAssertion;
        }
    }


    private List<CtMethod<?>> addAssertions(CtType<?> testClass, List<CtMethod<?>> testCases) throws IOException, ClassNotFoundException {
        CtType clone = testClass.clone();
        testClass.getPackage().addType(clone);
        LOGGER.info("Add observations points in passing tests.");
        LOGGER.info("Instrumentation...");
        final List<CtMethod<?>> testCasesWithLogs = testCases.stream()
                .map(ctMethod -> {
                            DSpotUtils.printProgress(testCases.indexOf(ctMethod), testCases.size());
                            return AssertGeneratorHelper.createTestWithLog(ctMethod,
                                    this.originalClass.getPackage().getQualifiedName()
                            );
                        }
                ).collect(Collectors.toList());
        final List<CtMethod<?>> testToRuns = new ArrayList<>();
        IntStream.range(0, 3).forEach(i -> testToRuns.addAll(
                testCasesWithLogs.stream()
                        .map(CtMethod::clone)
                        .map(ctMethod -> {
                            ctMethod.setSimpleName(ctMethod.getSimpleName() + i);
                            return ctMethod;
                        })
                        .map(ctMethod -> {
                            clone.addMethod(ctMethod);
                            return ctMethod;
                        })
                        .collect(Collectors.toList())
        ));
        ObjectLog.reset();
        LOGGER.info("Run instrumented tests. ({})", testToRuns.size());
        final TestListener result = TestCompiler.compileAndRun(clone,
                this.compiler,
                testToRuns,
                this.configuration
        );
        if (result == null || !result.getFailingTests().isEmpty()) {
            return Collections.emptyList();
        } else {
            Map<String, Observation> observations = ObjectLog.getObservations();
            LOGGER.info("Generating assertions...");
            return testCases.stream()
                    .map(ctMethod -> this.buildTestWithAssert(ctMethod, observations))
                    .collect(Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    private CtMethod<?> buildTestWithAssert(CtMethod test, Map<String, Observation> observations) {
        CtMethod testWithAssert = AmplificationHelper.cloneMethodTest(test, "");
        int numberOfAddedAssertion = 0;
        List<CtStatement> statements = Query.getElements(testWithAssert, new TypeFilter(CtStatement.class));
        for (String id : observations.keySet()) {
            if (!id.split("__")[0].equals(testWithAssert.getSimpleName())) {
                continue;
            }
            final List<CtStatement> assertStatements = AssertBuilder.buildAssert(factory,
                    observations.get(id).getNotDeterministValues(),
                    observations.get(id).getObservationValues());

            if (assertStatements.stream()
                    .map(Object::toString)
                    .map("// AssertGenerator add assertion\n"::concat)
                    .anyMatch(testWithAssert.getBody().getLastStatement().toString()::equals)) {
                continue;
            }
            int line = Integer.parseInt(id.split("__")[1]);
                CtStatement lastStmt = null;
                for (CtStatement statement : assertStatements) {
                    DSpotUtils.addComment(statement, "AssertGenerator add assertion", CtComment.CommentType.INLINE);
                    try {
                        CtStatement stmt = statements.get(line);
                        if (lastStmt == null) {
                            lastStmt = stmt;
                        }
                        if (stmt instanceof CtBlock) {
                            break;
                        }
                        if (stmt instanceof CtInvocation &&
                                !AssertGeneratorHelper.isVoidReturn((CtInvocation) stmt) &&
                                stmt.getParent() instanceof CtBlock) {
                            CtInvocation invocationToBeReplaced = (CtInvocation) stmt.clone();
                            final CtLocalVariable localVariable = factory.createLocalVariable(
                                    invocationToBeReplaced.getType(), "o_" + id.split("___")[0], invocationToBeReplaced
                            );
                            stmt.replace(localVariable);
                            DSpotUtils.addComment(localVariable, "AssertGenerator create local variable with return value of invocation", CtComment.CommentType.INLINE);
                            localVariable.setParent(stmt.getParent());
                            if (id.endsWith("end")) {
                                testWithAssert.getBody().insertEnd(statement);
                            } else {
                                localVariable.insertAfter(statement);
                            }
                            statements.remove(line);
                            statements.add(line, localVariable);
                        } else {
                            if (id.endsWith("end")) {
                                stmt.getParent(CtBlock.class).insertEnd(statement);
                            } else {
                                lastStmt.insertAfter(statement);
                            }
                        }
                        lastStmt = statement;
                        numberOfAddedAssertion++;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        }
        Counter.updateAssertionOf(testWithAssert, numberOfAddedAssertion);
        if (!testWithAssert.equals(test)) {
            AmplificationHelper.getAmpTestToParent().put(testWithAssert, test);
            return testWithAssert;
        } else {
            return null;
        }
    }

    protected CtMethod<?> makeFailureTest(CtMethod<?> test, Failure failure) {
        CtMethod cloneMethodTest = AmplificationHelper.cloneMethodTest(test, "");
        cloneMethodTest.setSimpleName(test.getSimpleName());
        Factory factory = cloneMethodTest.getFactory();

        Throwable exception = failure.getException();
        if (exception instanceof TestTimedOutException || // TestTimedOutException means infinite loop
                exception instanceof AssertionError) { // AssertionError means that some assertion remained in the test: TODO
            return null;
        }

        Class exceptionClass;
        if (exception == null) {
            exceptionClass = Exception.class;
        } else {
            exceptionClass = exception.getClass();
        }

        CtTry tryBlock = factory.Core().createTry();
        tryBlock.setBody(cloneMethodTest.getBody());
        String snippet = "org.junit.Assert.fail(\"" + test.getSimpleName() + " should have thrown " + exceptionClass.getSimpleName() + "\")";
        tryBlock.getBody().addStatement(factory.Code().createCodeSnippetStatement(snippet));
        DSpotUtils.addComment(tryBlock, "AssertGenerator generate try/catch block with fail statement", CtComment.CommentType.INLINE);

        CtCatch ctCatch = factory.Core().createCatch();
        CtTypeReference exceptionType = factory.Type().createReference(exceptionClass);
        ctCatch.setParameter(factory.Code().createCatchVariable(exceptionType, "eee"));

        ctCatch.setBody(factory.Core().createBlock());

        List<CtCatch> catchers = new ArrayList<>(1);
        catchers.add(ctCatch);
        tryBlock.setCatchers(catchers);

        CtBlock body = factory.Core().createBlock();
        body.addStatement(tryBlock);

        cloneMethodTest.setBody(body);
        cloneMethodTest.setSimpleName(cloneMethodTest.getSimpleName() + "_failAssert" + (numberOfFail++));
        Counter.updateAssertionOf(cloneMethodTest, 1);

        AmplificationHelper.getAmpTestToParent().put(cloneMethodTest, test);

        return cloneMethodTest;
    }
}
