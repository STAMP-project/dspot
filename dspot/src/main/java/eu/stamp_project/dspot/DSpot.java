package eu.stamp_project.dspot;

import eu.stamp_project.utils.configuration.AmplificationSetup;
import eu.stamp_project.utils.configuration.DSpotState;
import eu.stamp_project.utils.configuration.InitializeDSpot;
import eu.stamp_project.utils.configuration.TestTuple;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.GlobalReport;
import eu.stamp_project.utils.report.error.Error;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static eu.stamp_project.utils.report.error.ErrorEnum.ERROR_ASSERT_AMPLIFICATION;
import static eu.stamp_project.utils.report.error.ErrorEnum.ERROR_INPUT_AMPLIFICATION;
import static eu.stamp_project.utils.report.error.ErrorEnum.ERROR_SELECTION;

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

    public DSpot(InputConfiguration inputConfiguration){
        InitializeDSpot initializeDSpot = new InitializeDSpot();
        initializeDSpot.init(inputConfiguration);
        dSpotState = initializeDSpot.getDSpotState();
        setup(dSpotState);
    }

    public DSpot(DSpotState dSpotState) {
        setup(dSpotState);
    }

    private void setup(DSpotState configuration){
        this.dSpotState = configuration;
        setup = new AmplificationSetup(configuration);
        LOGGER = configuration.getLogger();
        globalNumberOfSelectedAmplification = 0;
        GLOBAL_REPORT = configuration.getGlobalReport();
    }

    public void run() {
        for (CtType<?> testClassToBeAmplified : dSpotState.getTestClassesToBeAmplified()) {
            TestTuple tuple = setup.preAmplification(testClassToBeAmplified, dSpotState.getTestMethodsToBeAmplifiedNames());
            final List<CtMethod<?>> amplifiedTestMethods = amplification(tuple.testClassToBeAmplified,tuple.testMethodsToBeAmplified);
            setup.postAmplification(testClassToBeAmplified,amplifiedTestMethods);
            globalNumberOfSelectedAmplification = 0;
        }
        setup.report(setup.getAmplifiedTestClasses());
    }

    private List<CtMethod<?>>  amplification(CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTestMethodsToKeep = onlyAssertionGeneration(testClassToBeAmplified,testMethodsToBeAmplified);
        if (dSpotState.getInputAmplDistributor().shouldBeRun()) {
            fullyAmplifyAllMethods(testClassToBeAmplified,testMethodsToBeAmplified,amplifiedTestMethodsToKeep);
        }
        return amplifiedTestMethodsToKeep;
    }

    private List<CtMethod<?>> onlyAssertionGeneration(CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified){
        final List<CtMethod<?>> selectedToBeAmplified;
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            selectedToBeAmplified = setup.firstSelectorSetup(testClassToBeAmplified,testMethodsToBeAmplified);

            // generate tests with additional assertions
            final List<CtMethod<?>> assertionAmplifiedTestMethods = this.assertionAmplification(testClassToBeAmplified,
                    selectedToBeAmplified);

            // keep tests that improve the test suite
            amplifiedTestMethodsToKeep = selectOnlyAssertionGeneration(assertionAmplifiedTestMethods);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return amplifiedTestMethodsToKeep;
    }

    // iteratively generate tests with input modification and associated new assertions for all methods
    private void fullyAmplifyAllMethods(CtType<?> testClassToBeAmplified,List<CtMethod<?>> testMethodsToBeAmplified,
                                        List<CtMethod<?>> amplifiedTestMethodsToKeep){
        LOGGER.info("Applying Input-amplification and Assertion-amplification test by test.");
        for (int i = 0; i < testMethodsToBeAmplified.size(); i++) {
            CtMethod test = testMethodsToBeAmplified.get(i);
            LOGGER.info("Amplification of {}, ({}/{})", test.getSimpleName(), i + 1, testMethodsToBeAmplified.size());

            // tmp list for current test methods to be amplified
            // this list must be a implementation that support remove / clear methods
            List<CtMethod<?>> currentTestList = new ArrayList<>();
            currentTestList.add(test);
            final List<CtMethod<?>> amplifiedTests = new ArrayList<>();
            for (int j = 0; j < dSpotState.getNbIteration() ; j++) {
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
     * Amplification of test methods
     *
     * DSpot combines the different kinds of I-Amplification iteratively: at each iteration all kinds of
     * I-Amplification are applied, resulting in new tests. From one iteration to another, DSpot reuses the
     * previously amplified tests, and further applies I-Amplification.
     *
     * @param testClassToBeAmplified        Test class
     * @param currentTestListToBeAmplified  Methods to amplify
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
            selectedToBeAmplified = setup.fullSelectorSetup(testClassToBeAmplified,currentTestListToBeAmplified);

            // amplify tests and shrink amplified set with inputAmplDistributor
            inputAmplifiedTests = dSpotState.getInputAmplDistributor().inputAmplify(selectedToBeAmplified, currentIteration);

            // add assertions to input modified tests
            currentTestList = this.assertionAmplification(testClassToBeAmplified, inputAmplifiedTests);

            // keep tests that improve the test suite
            selectFullAmplification(currentTestList,amplifiedTests);
        } catch (AmplificationException e) {
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
            testsWithAssertions = dSpotState.getAssertionGenerator().assertionAmplification(classTest, testMethods);
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
        LOGGER.info("Assertion amplification: {} test method(s) has been successfully amplified.",
                amplifiedPassingTests.size());
        return amplifiedPassingTests;
    }

    private List<CtMethod<?>>  selectOnlyAssertionGeneration(List<CtMethod<?>> assertionAmplifiedTestMethods)
            throws Exception {
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            amplifiedTestMethodsToKeep = dSpotState.getTestSelector().selectToKeep(assertionAmplifiedTestMethods);
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            throw new Exception();
        }
        this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
        LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})",
                amplifiedTestMethodsToKeep.size(), this.globalNumberOfSelectedAmplification);

        return amplifiedTestMethodsToKeep;
    }

    private void selectFullAmplification(List<CtMethod<?>> currentTestList,List<CtMethod<?>> amplifiedTests)
            throws AmplificationException {
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            amplifiedTestMethodsToKeep = dSpotState.getTestSelector().selectToKeep(currentTestList);
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            throw new AmplificationException("");
        }
        LOGGER.info("{} amplified test methods has been selected to be kept.", amplifiedTestMethodsToKeep.size());
        amplifiedTests.addAll(amplifiedTestMethodsToKeep);
    }
}
