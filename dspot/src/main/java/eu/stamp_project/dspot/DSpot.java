package eu.stamp_project.dspot;

import eu.stamp_project.dspot.common.configuration.*;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationException;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.report.GlobalReport;
import eu.stamp_project.dspot.common.report.error.Error;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static eu.stamp_project.dspot.common.report.error.ErrorEnum.ERROR_ASSERT_AMPLIFICATION;
import static eu.stamp_project.dspot.common.report.error.ErrorEnum.ERROR_INPUT_AMPLIFICATION;
import static eu.stamp_project.dspot.common.report.error.ErrorEnum.ERROR_SELECTION;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private DSpotState dSpotState;
    private AmplificationSetup setup;
    private int globalNumberOfSelectedAmplification;
    private Logger LOGGER;
    private GlobalReport GLOBAL_REPORT;

    public DSpot(UserInput userInput) {
        InitializeDSpot initializeDSpot = new InitializeDSpot();
        initializeDSpot.init(userInput);
        dSpotState = initializeDSpot.getDSpotState();
        setup(dSpotState);
    }

    public DSpot(DSpotState dSpotState) {
        setup(dSpotState);
    }

    private void setup(DSpotState configuration) {
        this.dSpotState = configuration;
        setup = new AmplificationSetup(configuration);
        LOGGER = configuration.getLogger();
        globalNumberOfSelectedAmplification = 0;
        GLOBAL_REPORT = configuration.getGlobalReport();
    }

    public void run() {
        for (CtType<?> testClassToBeAmplified : dSpotState.getTestClassesToBeAmplified()) {
            TestTuple tuple = setup.preAmplification(testClassToBeAmplified, dSpotState.getTestMethodsToBeAmplifiedNames());
            final List<CtMethod<?>> amplifiedTestMethods;
            if (dSpotState.isDevFriendlyAmplification()) {
                amplifiedTestMethods =
                        new DevFriendlyAmplification(this, dSpotState, setup, LOGGER, GLOBAL_REPORT)
                                .devFriendlyAmplification(tuple.testClassToBeAmplified, tuple.testMethodsToBeAmplified);
            } else {
                amplifiedTestMethods = amplification(tuple.testClassToBeAmplified,
                        tuple.testMethodsToBeAmplified);
            }
            setup.postAmplification(testClassToBeAmplified, amplifiedTestMethods);
            globalNumberOfSelectedAmplification = 0;
        }
        setup.report(setup.getAmplifiedTestClasses());
    }

    private List<CtMethod<?>> amplification(CtType<?> testClassToBeAmplified,
                                            List<CtMethod<?>> testMethodsToBeAmplified) {
        List<CtMethod<?>> amplifiedTestMethodsToKeep = setupSelector(testClassToBeAmplified, testMethodsToBeAmplified);

        if (!dSpotState.isOnlyInputAmplification()) {
            amplifiedTestMethodsToKeep = onlyAssertionGeneration(testClassToBeAmplified, amplifiedTestMethodsToKeep);
        }

        if (dSpotState.getInputAmplDistributor().shouldBeRun()) {
            fullyAmplifyAllMethods(testClassToBeAmplified, testMethodsToBeAmplified, amplifiedTestMethodsToKeep);
        }
        return amplifiedTestMethodsToKeep;
    }

    public List<CtMethod<?>> setupSelector(CtType<?> testClassToBeAmplified,
                                          List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> selectedToBeAmplified;
        try {
            selectedToBeAmplified = setup.firstSelectorSetup(testClassToBeAmplified, testMethodsToBeAmplified);
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return selectedToBeAmplified;

    }

    private List<CtMethod<?>> onlyAssertionGeneration(CtType<?> testClassToBeAmplified, List<CtMethod<?>> selectedToBeAmplified) {
        final List<CtMethod<?>> amplifiedTestMethodsToKeep = new ArrayList<>();
        try {
            // generate tests with additional assertions
            final List<CtMethod<?>> assertionAmplifiedTestMethods = this.assertionAmplification(testClassToBeAmplified,
                    selectedToBeAmplified);

            // keep tests that improve the test suite
            selectImprovingTestCases(assertionAmplifiedTestMethods, amplifiedTestMethodsToKeep);

            this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
            LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsToKeep.size(),
                    this.globalNumberOfSelectedAmplification);
        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return amplifiedTestMethodsToKeep;
    }

    /**
     * Iteratively generate tests with input modification and associated new assertions for all methods.
     * <p>
     * DSpot combines the different kinds of I-Amplification iteratively: at each iteration all kinds of
     * I-Amplification are applied, resulting in new tests. From one iteration to another, DSpot reuses the
     * previously amplified tests, and further applies I-Amplification.
     * @param testClassToBeAmplified       Test class
     * @param testMethodsToBeAmplified     Methods to amplify
     * @param amplifiedTestMethodsToKeep   Collection of amplified tests that will be kept
     */
    private void fullyAmplifyAllMethods(CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified,
                                        List<CtMethod<?>> amplifiedTestMethodsToKeep) {
        if (dSpotState.isOnlyInputAmplification()) {
            LOGGER.info("Applying only Input-amplification test by test.");
        } else {
            LOGGER.info("Applying Input-amplification and Assertion-amplification test by test.");
        }
        for (int i = 0; i < testMethodsToBeAmplified.size(); i++) {
            CtMethod test = testMethodsToBeAmplified.get(i);
            LOGGER.info("Amplification of {}, ({}/{})", test.getSimpleName(), i + 1, testMethodsToBeAmplified.size());

            // tmp list for current test methods to be amplified
            // this list must be a implementation that support remove / clear methods
            List<CtMethod<?>> currentTestList = new ArrayList<>();
            currentTestList.add(test);
            final List<CtMethod<?>> amplifiedTests = new ArrayList<>();
            for (int j = 0; j < dSpotState.getNbIteration(); j++) {
                LOGGER.info("iteration {} / {}", j, dSpotState.getNbIteration());
                currentTestList = this.fullAmplification(testClassToBeAmplified, currentTestList, amplifiedTests, j);
            }
            amplifiedTestMethodsToKeep.addAll(amplifiedTests);
            this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
            LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTests.size(),
                    this.globalNumberOfSelectedAmplification);
        }
    }

    /**
     * Generate tests with input modification and associated new assertions for all methods.
     * (one iteration)
     *
     * @param testClassToBeAmplified       Test class
     * @param currentTestListToBeAmplified Methods to amplify
     * @return Valid amplified tests
     */
    public List<CtMethod<?>> fullAmplification(CtType<?> testClassToBeAmplified,
                                               List<CtMethod<?>> currentTestListToBeAmplified,
                                               List<CtMethod<?>> amplifiedTests,
                                               int currentIteration) {
        final List<CtMethod<?>> selectedToBeAmplified;
        final List<CtMethod<?>> inputAmplifiedTests;
        final List<CtMethod<?>> currentTestList;
        try {
            selectedToBeAmplified = setup.fullSelectorSetup(testClassToBeAmplified, currentTestListToBeAmplified);

            // amplify tests and shrink amplified set with inputAmplDistributor
            inputAmplifiedTests = dSpotState.getInputAmplDistributor().inputAmplify(selectedToBeAmplified, currentIteration);

            if (dSpotState.isOnlyInputAmplification()) {
                currentTestList = dSpotState.getTestCompiler().compileRunAndDiscardUncompilableAndFailingTestMethods(
                        testClassToBeAmplified,
                        inputAmplifiedTests,
                        dSpotState.getCompiler()
                );
                LOGGER.info("Amplification: {} test method(s) has been successfully amplified.",
                        currentTestList.size());
            } else {
                // add assertions to input modified tests
                currentTestList = this.assertionAmplification(testClassToBeAmplified, inputAmplifiedTests);
            }

            // keep tests that improve the test suite
            selectImprovingTestCases(currentTestList, amplifiedTests);
        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_INPUT_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        return currentTestList;
    }

    private List<CtMethod<?>> assertionAmplification(CtType<?> classTest, List<CtMethod<?>> testMethods) {
        final List<CtMethod<?>> testsWithAssertions;
        try {
            testsWithAssertions = dSpotState.getAssertionGenerator().removeAndAmplifyAssertions(classTest, testMethods);
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        if (testsWithAssertions.isEmpty()) {
            return testsWithAssertions;
        }

        // final check on A-amplified test, see if they all pass. if they don't, we just discard them.
        final List<CtMethod<?>> amplifiedPassingTests =
                dSpotState.getTestCompiler().compileRunAndDiscardUncompilableAndFailingTestMethods(
                        classTest,
                        testsWithAssertions,
                        dSpotState.getCompiler()
                );
        LOGGER.info("Assertion amplification: {} test method(s) have been successfully amplified.",
                amplifiedPassingTests.size());
        return amplifiedPassingTests;
    }

    /**
     * Keeps the test cases from @param currentTestList that improve the test suite according to the test selector.
     * Adds the kept test cases to @param amplifiedTests.
     * @param currentTestList Test cases that will be filtered
     * @param amplifiedTests List that good test cases will be added to
     * @throws AmplificationException
     */
    public void selectImprovingTestCases(List<CtMethod<?>> currentTestList, List<CtMethod<?>> amplifiedTests)
            throws AmplificationException  {
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            amplifiedTestMethodsToKeep = dSpotState.getTestSelector().selectToKeep(currentTestList);
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            throw new AmplificationException("");
        }
        LOGGER.info("{} amplified test method(s) have been selected to be kept.", amplifiedTestMethodsToKeep.size());
        amplifiedTests.addAll(amplifiedTestMethodsToKeep);
    }

}
