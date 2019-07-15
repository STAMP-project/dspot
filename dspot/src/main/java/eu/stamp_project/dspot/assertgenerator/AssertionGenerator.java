package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.dspot.assertgenerator.components.AssertionRemover;
import eu.stamp_project.dspot.assertgenerator.components.TestMethodReconstructor;
import eu.stamp_project.dspot.assertgenerator.components.TryCatchFailGenerator;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.program.InputConfiguration;
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

    private InputConfiguration configuration;

    private DSpotCompiler compiler;

    private AssertionRemover assertionRemover;

    private TryCatchFailGenerator tryCatchFailGenerator;

    private TestMethodReconstructor testMethodReconstructor;

    public AssertionGenerator(InputConfiguration configuration, DSpotCompiler compiler) {
        this.configuration = configuration;
        this.compiler = compiler;
        this.assertionRemover = new AssertionRemover();
        this.tryCatchFailGenerator = new TryCatchFailGenerator();
    }

    /**
     * Adds new assertions in multiple tests.
     * <p>
     * <p>Details of the assertions generation in {@link #innerAssertionAmplification(CtType, List)}.
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

        // remove existing assertions from cloned test methods
        List<CtMethod<?>> testsWithoutAssertions = tests.stream()
                .map(this.assertionRemover::removeAssertion)
                .collect(Collectors.toList());
        testsWithoutAssertions.forEach(cloneClass::addMethod);

        // set up testMethodReconstructor for use in innerAssertionAmplification
        this.testMethodReconstructor = new TestMethodReconstructor(
                testClass,
                this.configuration,
                compiler,
                this.assertionRemover.getVariableAssertedPerTestMethod()
        );
        final List<CtMethod<?>> amplifiedTestsWithAssertions =
                this.innerAssertionAmplification(cloneClass, testsWithoutAssertions);
        if (amplifiedTestsWithAssertions.isEmpty()) {
            LOGGER.info("Could not generate any test with assertions");
        } else {
            LOGGER.info("{} new tests with assertions generated", amplifiedTestsWithAssertions.size());
        }
        return amplifiedTestsWithAssertions;
    }

    private boolean checkMethodName(String patternMethodName, String methodNameToBeChecked) {
        return Pattern.compile(patternMethodName + "(\\[\\d+\\])?").matcher(methodNameToBeChecked).matches();
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
    private List<CtMethod<?>> innerAssertionAmplification(CtType testClass, List<CtMethod<?>> tests) {

        // check if input amplified tests throw new exceptions
        LOGGER.info("Run tests. ({})", tests.size());
        final TestResult testResult;
        try {
            //Add parallel test execution support (JUnit4, JUnit5) for execution method (CMD, Maven)
            CloneHelper.addParallelExecutionAnnotation(testClass, tests);
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
        final List<String> failuresMethodName = testResult.getFailingTests()
                .stream()
                .map(failure -> failure.testCaseName)
                .collect(Collectors.toList());
        final List<String> passingTestsName = testResult.getPassingTests();
        final List<CtMethod<?>> generatedTestWithAssertion = new ArrayList<>();

        // add assertions on passing tests
        if (!passingTestsName.isEmpty()) {
            LOGGER.info("{} test pass, generating assertion...", passingTestsName.size());
            final List<CtMethod<?>> passingTestMethods = tests.stream()
                    .filter(ctMethod ->
                            passingTestsName.stream()
                                    .anyMatch(passingTestName -> checkMethodName(ctMethod.getSimpleName(), passingTestName))
                    ).collect(Collectors.toList());
            List<CtMethod<?>> passingTests = this.testMethodReconstructor.addAssertions(testClass,
                    passingTestMethods)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            generatedTestWithAssertion.addAll(passingTests);
        }

        // add try/catch block with fail statement in failing tests
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
                generatedTestWithAssertion.addAll(failingTests);
            }
        }
        return generatedTestWithAssertion;
    }
}
