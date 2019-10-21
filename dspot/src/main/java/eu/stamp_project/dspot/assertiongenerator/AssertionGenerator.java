package eu.stamp_project.dspot.assertiongenerator;

import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionRemover;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.MethodReconstructor;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.TryCatchFailGenerator;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
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

    public AssertionGenerator(double delta, DSpotCompiler compiler, TestCompiler testCompiler) {
        this.delta = delta;
        this.compiler = compiler;
        this.assertionRemover = new AssertionRemover();
        this.tryCatchFailGenerator = new TryCatchFailGenerator();
        this.testCompiler = testCompiler;
    }

    /**
     * Removes old assertions and adds new assertions and fail statements in multiple tests.
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
        CtType cloneClass = testClass.clone();
        cloneClass.setParent(testClass.getParent());
        List<CtMethod<?>> testsWithoutAssertions = removeAssertions(tests,cloneClass);

        // set up methodReconstructor for use in assertPassingAndFailingTests
        this.methodReconstructor = new MethodReconstructor(
                delta,
                testClass,
                compiler,
                this.assertionRemover.getVariableAssertedPerTestMethod(),
                this.testCompiler
        );
        final List<CtMethod<?>> amplifiedTestsWithAssertions =
                this.assertPassingAndFailingTests(cloneClass, testsWithoutAssertions);
        decideLoggerOutput(amplifiedTestsWithAssertions);
        return amplifiedTestsWithAssertions;
    }

    // remove existing assertions from cloned test methods
    private List<CtMethod<?>> removeAssertions(List<CtMethod<?>> tests,CtType cloneClass){
        List<CtMethod<?>> testsWithoutAssertions = tests.stream()
                .map(this.assertionRemover::removeAssertion)
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
            testResult = this.testCompiler.compileAndRun(testClass,
                    this.compiler,
                    tests
            );
        } catch (AmplificationException e) {
            LOGGER.warn("Error when executing tests before Assertion Amplification:");
            e.printStackTrace();
            return Collections.emptyList();
        }
        final List<CtMethod<?>> generatedTestWithAssertion = new ArrayList<>();
        generatedTestWithAssertion.addAll(addAssertionsOnPassingTests(testResult,tests,testClass));
        generatedTestWithAssertion.addAll(addFailStatementOnFailingTests(testResult,tests));
        return generatedTestWithAssertion;
    }

    private List<CtMethod<?>> addAssertionsOnPassingTests(TestResult testResult,List<CtMethod<?>> tests,CtType testClass){
        final List<CtMethod<?>> generatedTestWithAssertion = new ArrayList<>();
        final List<String> passingTestsName = testResult.getPassingTests();
        if (!passingTestsName.isEmpty()) {
            LOGGER.info("{} test pass, generating assertion...", passingTestsName.size());
            final List<CtMethod<?>> passingTestMethods = tests.stream()
                    .filter(ctMethod ->
                            passingTestsName.stream()
                                    .anyMatch(passingTestName -> checkMethodName(ctMethod.getSimpleName(), passingTestName))
                    ).collect(Collectors.toList());
            List<CtMethod<?>> passingTests = this.methodReconstructor.addAssertions(testClass,
                    passingTestMethods)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            generatedTestWithAssertion.addAll(passingTests);
            return generatedTestWithAssertion;
        }
        return new ArrayList<>();
    }

    private boolean checkMethodName(String patternMethodName, String methodNameToBeChecked) {
        return Pattern.compile(patternMethodName + "(\\[\\d+\\])?").matcher(methodNameToBeChecked).matches();
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
                            failuresMethodName.contains(ctMethod.getSimpleName()))
                    .map(ctMethod ->
                            this.tryCatchFailGenerator
                                    .surroundWithTryCatchFail(ctMethod, testResult.getFailureOf(ctMethod.getSimpleName()))
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

