package eu.stamp_project.dspot.assertiongenerator;

import eu.stamp_project.dspot.common.configuration.TestTuple;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationException;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionRemover;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.MethodReconstructor;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.TryCatchFailGenerator;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.compilation.TestCompiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 12/02/16
 * Time: 10:31
 */

public class AssertionGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionGenerator.class);

    private DSpotCompiler compiler;

    private AssertionRemover assertionRemover;

    private TryCatchFailGenerator tryCatchFailGenerator;

    private MethodReconstructor methodReconstructor;

    private double delta;

    private TestCompiler testCompiler;

    private final boolean devFriendlyAmplification;

    public AssertionGenerator(double delta, DSpotCompiler compiler, TestCompiler testCompiler) {
        this(delta,  compiler, testCompiler, false);
    }

    public AssertionGenerator(double delta, DSpotCompiler compiler, TestCompiler testCompiler, boolean devFriendlyAmplification) {
        this.delta = delta;
        this.compiler = compiler;
        this.assertionRemover = new AssertionRemover();
        this.tryCatchFailGenerator = new TryCatchFailGenerator();
        this.testCompiler = testCompiler;
        this.devFriendlyAmplification = devFriendlyAmplification;
    }

    public List<CtMethod<?>> removeAndAmplifyAssertions(CtType<?> testClass, List<CtMethod<?>> tests) {
        TestTuple testsWithOldAssertionsRemoved = removeAssertions(testClass, tests);
        return assertionAmplification(
                testsWithOldAssertionsRemoved.testClassToBeAmplified,
                testsWithOldAssertionsRemoved.testMethodsToBeAmplified);
    }

    /**
     * Adds new assertions and fail statements in multiple tests.
     * Details of the assertions generation in {@link #assertPassingAndFailingTests(CtType, List)}.
     *
     * @param testClass Test class
     * @param tests     Test methods to amplify
     * @return New amplified tests
     */
    public List<CtMethod<?>> assertionAmplification(CtType<?> testClass, List<CtMethod<?>> tests) {
        if (tests.isEmpty()) {
            return tests;
        }
        CtType<?> cloneClass = CloneHelper.cloneTestClassAndAddGivenTest(testClass, tests);

        // set up methodReconstructor for use in assertPassingAndFailingTests
        this.methodReconstructor = new MethodReconstructor(
                delta,
                testClass,
                compiler,
                this.assertionRemover.getVariableAssertedPerTestMethod(),
                this.testCompiler,
                this.devFriendlyAmplification
        );
        final List<CtMethod<?>> amplifiedTestsWithAssertions =
                this.assertPassingAndFailingTests(cloneClass, tests);
        decideLoggerOutput(amplifiedTestsWithAssertions);
        return amplifiedTestsWithAssertions;
    }

    /**
     * Removes old assertions in multiple tests.
     * @param testClass Test class
     * @param tests     Test methods in which to remove assertions
     * @return Test methods without assertions
     */
    public TestTuple removeAssertions(CtType<?> testClass, List<CtMethod<?>> tests) {
        if (tests.isEmpty()) {
            return new TestTuple(testClass, tests);
        }
        CtType<?> cloneClass = testClass.clone();
        testClass.getPackage().addType(cloneClass);
        if (devFriendlyAmplification) {
            return new TestTuple(cloneClass, removeAssertionsCompletely(tests,cloneClass));
        } else {
            return new TestTuple(cloneClass, removeAssertions(tests, cloneClass));
        }
    }

    /**
     * Uses {@link AssertionRemover#removeAssertions(CtMethod, boolean)} to remove existing assertions from cloned
     * test methods, but leaves the arguments of the assertions
     * @param tests
     * @param cloneClass
     * @return
     */
    private List<CtMethod<?>> removeAssertions(List<CtMethod<?>> tests, CtType<?> cloneClass){
        List<CtMethod<?>> testsWithoutAssertions = tests.stream()
                .map(test -> this.assertionRemover.removeAssertions(test, true))
                .collect(Collectors.toList());
        testsWithoutAssertions.forEach(cloneClass::addMethod);
        return testsWithoutAssertions;
    }

    /**
     * Uses {@link AssertionRemover#removeAssertions(CtMethod, boolean)} to remove existing assertions and their
     * arguments from cloned test methods
     * @param tests
     * @param cloneClass
     * @return
     */
    private List<CtMethod<?>> removeAssertionsCompletely(List<CtMethod<?>> tests, CtType<?> cloneClass){
        List<CtMethod<?>> testsWithoutAssertions = tests.stream()
                .map(test -> this.assertionRemover.removeAssertions(test, false))
                .collect(Collectors.toList());
        testsWithoutAssertions.forEach(cloneClass::addMethod);
        return testsWithoutAssertions;
    }

    private void decideLoggerOutput(List<CtMethod<?>> amplifiedTestsWithAssertions){
        if (amplifiedTestsWithAssertions.isEmpty()) {
            LOGGER.info("Could not generate any test with assertions");
        } else {
            LOGGER.info("{} new tests with assertions generated", amplifiedTestsWithAssertions.size());
        }
    }

    /**
     * Generates assertions and try/catch/fail blocks for multiple tests.
     *
     * Assertion Amplification process.
     * <ol>
     * <li>Instrumentation to collect the state of the program after execution (but before assertions).</li>
     * <li>Collection of actual values by running the tests.</li>
     * <li>Generation of new assertions in place of observation points.
     * Generation of catch blocks if a test raises an exception.</li>
     * </ol>
     * The details of the first two points are in {@link MethodReconstructor#addAssertions(CtType, List)}.
     *
     * @param testClass Test class
     * @param tests     Test methods
     * @return New tests with new assertions
     */
    private List<CtMethod<?>> assertPassingAndFailingTests(CtType testClass, List<CtMethod<?>> tests) {
        LOGGER.info("Run tests. ({})", tests.size());
        final TestResult testResult;
        try {

            //Add parallel test execution support (JUnit4, JUnit5) for execution method (CMD, Maven)
            CloneHelper.addParallelExecutionAnnotation(testClass, tests);
            testResult = this.testCompiler.compileAndRun(
                    testClass,
                    this.compiler,
                    tests
            );
        } catch (AmplificationException e) {
            LOGGER.warn("Error when executing tests before Assertion Amplification:");
            e.printStackTrace();
            return Collections.emptyList();
        }
        final List<CtMethod<?>> generatedTestWithAssertion = new ArrayList<>();
        generatedTestWithAssertion.addAll(addAssertionsOnPassingTests(testResult, tests, testClass));
        generatedTestWithAssertion.addAll(addFailStatementOnFailingTests(testResult, tests));
        return generatedTestWithAssertion;
    }

    private List<CtMethod<?>> addAssertionsOnPassingTests(TestResult testResult, List<CtMethod<?>> tests, CtType testClass){
        final List<CtMethod<?>> generatedTestWithAssertion = new ArrayList<>();
        final Set<String> passingTestsName = testResult.getPassingTests();
        if (!passingTestsName.isEmpty()) {
            LOGGER.info("{} test pass, generating assertion...", passingTestsName.size());
            final List<CtMethod<?>> passingTestMethods = tests.stream()
                    .filter(ctMethod ->
                            passingTestsName.stream()
                                    .anyMatch(passingTestName -> AmplificationHelper.checkMethodName(testClass.getQualifiedName() + "#" + ctMethod.getSimpleName(), passingTestName))
                    ).collect(Collectors.toList());
            List<CtMethod<?>> passingTests = this.methodReconstructor.addAssertions(testClass, passingTestMethods)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            generatedTestWithAssertion.addAll(passingTests);
            return generatedTestWithAssertion;
        }
        return new ArrayList<>();
    }

    // add try/catch block with fail statement in failing tests
    private List<CtMethod<?>> addFailStatementOnFailingTests(TestResult testResult, List<CtMethod<?>> tests){
        final List<String> failuresMethodName = testResult.getFailingTests()
                .stream()
                .map(failure -> failure.testCaseName)
                .collect(Collectors.toList());
        if (!failuresMethodName.isEmpty()) {
            LOGGER.info("{} test fail, generating try/catch/fail blocks...", failuresMethodName.size());
            final List<CtMethod<?>> failingTests = tests.stream()
                    .filter(ctMethod ->
                            failuresMethodName.contains(ctMethod.getParent(CtClass.class).getQualifiedName() + "#" + ctMethod.getSimpleName()))
                    .map(ctMethod ->
                            this.tryCatchFailGenerator
                                    .surroundWithTryCatchFail(ctMethod, testResult.getFailureOf(ctMethod.getParent(CtClass.class).getQualifiedName() + "#" + ctMethod.getSimpleName()))
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!failingTests.isEmpty()) {
                return failingTests;
            }
        }
        return new ArrayList<>();
    }
}

