package eu.stamp_project.dspot;

import eu.stamp_project.dspot.common.configuration.AmplificationSetup;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.TestTuple;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationException;
import eu.stamp_project.dspot.common.report.GlobalReport;
import eu.stamp_project.dspot.common.report.error.Error;
import eu.stamp_project.testrunner.EntryPoint;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.stamp_project.dspot.common.report.error.ErrorEnum.ERROR_ASSERT_AMPLIFICATION;

public class DevFriendlyAmplification {

    private final DSpot dSpot;
    private final DSpotState dSpotState;
    private final AmplificationSetup setup;
    private final Logger LOGGER;
    private final GlobalReport GLOBAL_REPORT;

    public DevFriendlyAmplification(DSpot dSpot, DSpotState dSpotState, AmplificationSetup setup, Logger LOGGER,
                                    GlobalReport GLOBAL_REPORT) {
        this.dSpot = dSpot;
        this.dSpotState = dSpotState;
        this.setup = setup;
        this.LOGGER = LOGGER;
        this.GLOBAL_REPORT = GLOBAL_REPORT;
    }

    /**
     * Amplifies the test cases in a way suitable to present the results to developers.
     *
     * @param testClassToBeAmplified   Test class to be amplified
     * @param testMethodsToBeAmplified Test methods to be amplified
     * @return Amplified test methods
     */
    public List<CtMethod<?>> devFriendlyAmplification(CtType<?> testClassToBeAmplified,
                                                      List<CtMethod<?>> testMethodsToBeAmplified) {

        final List<CtMethod<?>> selectedToBeAmplified = dSpot
                .setupSelector(testClassToBeAmplified,
                        dSpotState.getTestFinder().findTestMethods(testClassToBeAmplified,Collections.emptyList()));

        // selectedToBeAmplified with all test class methods -> keep only ones matching testMethodsToBeAmplified
        final List<CtMethod<?>> methodsToAmplify =
                selectedToBeAmplified.stream().filter(testMethodsToBeAmplified::contains).collect(Collectors.toList());

        final List<CtMethod<?>> amplifiedTestMethodsToKeep = new ArrayList<>();
        amplifiedTestMethodsToKeep.addAll(ampRemoveAssertionsAddNewOnes(testClassToBeAmplified, methodsToAmplify));
        amplifiedTestMethodsToKeep.addAll(inputAmplification(testClassToBeAmplified, methodsToAmplify));
        return amplifiedTestMethodsToKeep;
    }

    /**
     * Path 1 of dev-friendly amplification: remove old assertions (if at the end of test case: completely, also
     * invocations inside assertions are removed) and then add single new assertions.
     * @param testClassToBeAmplified original test class
     * @param testMethodsToBeAmplified original test methods
     * @return amplified test methods
     */
    public List<CtMethod<?>> ampRemoveAssertionsAddNewOnes(CtType<?> testClassToBeAmplified,
                                                           List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;
        try {
            TestTuple testTuple;
            // Remove old assertions
            testTuple = dSpotState.getAssertionGenerator()
                    .removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);

            // Add new assertions
            amplifiedTests = dSpotState.getAssertionGenerator()
                    .assertionAmplification(testTuple.testClassToBeAmplified, testTuple.testMethodsToBeAmplified);
            classWithTestMethods = testTuple.testClassToBeAmplified;
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        return selectPassingAndImprovingTests(amplifiedTests,classWithTestMethods,1);
    }

    /**
     * Path 2 of dev-friendly amplification: remove old assertions, amplify inputs and then add new assertions.
     * The new assertions assert values that changed through the input amplification.
     * @param testClassToBeAmplified original test class
     * @param testMethodsToBeAmplified original test methods
     * @return amplified test methods
     */
    public List<CtMethod<?>> inputAmplification(CtType<?> testClassToBeAmplified,
                                                List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;
        try {
            TestTuple testTuple;
            // Remove old assertions
            testTuple = dSpotState.getAssertionGenerator()
                    .removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);
            classWithTestMethods = testTuple.testClassToBeAmplified;

            // Amplify input
            List<CtMethod<?>> selectedForInputAmplification = setup
                    .fullSelectorSetup(classWithTestMethods, testTuple.testMethodsToBeAmplified);

            List<CtMethod<?>> inputAmplifiedTests = dSpotState.getInputAmplDistributor()
                    .inputAmplify(selectedForInputAmplification, 0);

            // Add new assertions
            amplifiedTests = dSpotState.getAssertionGenerator()
                    .assertionAmplification(classWithTestMethods, inputAmplifiedTests);

        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        return selectPassingAndImprovingTests(amplifiedTests,classWithTestMethods,2);
    }

    private List<CtMethod<?>> selectPassingAndImprovingTests(List<CtMethod<?>> amplifiedTests,
                                                             CtType<?> classWithTestMethods,
                                                             int path) {
        if (amplifiedTests.isEmpty()) {
            return Collections.emptyList();
        }
        final List<CtMethod<?>> amplifiedPassingTests = dSpotState.getTestCompiler()
                .compileRunAndDiscardUncompilableAndFailingTestMethods(classWithTestMethods, amplifiedTests, dSpotState
                        .getCompiler());

        // Keep tests that improve the test suite
        final List<CtMethod<?>> improvingTests = new ArrayList<>();
        try {
            dSpot.selectImprovingTestCases(amplifiedPassingTests, improvingTests);
        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        LOGGER.info("Dev friendly amplification, path {}: {} test method(s) have been successfully amplified.",
                path, improvingTests.size());
        return improvingTests;
    }

}
