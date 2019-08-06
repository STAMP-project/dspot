package eu.stamp_project.dspot.assertiongenerator.performancetest.performancetest_components;

import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.TestMethodReconstructor;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.TryCatchFailGenerator;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.utils.program.InputConfiguration;
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
import java.util.stream.Collectors;

public class AssertionGeneratorWithTime extends AssertionGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionGenerator.class);

    private InputConfiguration configuration;

    private DSpotCompiler compiler;

    public AssertionRemoverWithTime assertionRemover;

    public TryCatchFailGenerator tryCatchFailGenerator;

    public TestMethodReconstructorWithTime testMethodReconstructorWithTime;

    public AssertionGeneratorWithTime(InputConfiguration configuration, DSpotCompiler compiler) {
        super(configuration, compiler);
        this.configuration = configuration;
        this.compiler = compiler;
        this.assertionRemover = new AssertionRemoverWithTime();
        this.tryCatchFailGenerator = new TryCatchFailGenerator();
    }

    public void reset() {
        this.assertionRemover.reset();
        this.testMethodReconstructorWithTime.reset();
    }

    /**
     * Removes old assertions and adds new assertions and fail statements in multiple tests.
     * <p>
     * <p>Details of the assertions generation in {@link #assertPassingAndFailingTests(CtType, List)}.
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

        // set up testMethodReconstructorWithTime for use in assertPassingAndFailingTests
        this.testMethodReconstructorWithTime = new TestMethodReconstructorWithTime(
                testClass,
                this.configuration,
                compiler,
                this.assertionRemover.getVariableAssertedPerTestMethod()
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
     * <p>
     * <p>Assertion Amplification process.
     * <ol>
     * <li>Instrumentation to collect the state of the program after execution (but before assertions).</li>
     * <li>Collection of actual values by running the tests.</li>
     * <li>Generation of new assertions in place of observation points.
     * Generation of catch blocks if a test raises an exception.</li>
     * </ol>
     * The details of the first two points are in {@link TestMethodReconstructor#addAssertions(CtType, List)}.
     *
     * @param testClass Test class
     * @param tests     Test methods
     * @return New tests with new assertions
     */
    private List<CtMethod<?>> assertPassingAndFailingTests(CtType testClass, List<CtMethod<?>> tests) {
        LOGGER.info("Run tests. ({})", tests.size());
        final TestResult testResult;
        try {
            testResult = TestCompiler.compileAndRun(testClass,
                    this.compiler,
                    tests,
                    this.configuration
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
            List<CtMethod<?>> passingTests = this.testMethodReconstructorWithTime.addAssertions(testClass,
                    tests.stream()
                            .filter(ctMethod -> passingTestsName.contains(ctMethod.getSimpleName()))
                            .collect(Collectors.toList()))
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (passingTests != null) {
                return generatedTestWithAssertion;
            }
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
