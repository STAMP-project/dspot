package eu.stamp_project.dspot;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.options.InputConfiguration;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * User: Simon
 * Date: 03/12/15
 * Time: 13:52
 */
public class Amplification {

    private static final Logger LOGGER = LoggerFactory.getLogger(Amplification.class);

    private AssertionGenerator assertionGenerator;

    private DSpotCompiler compiler;

    private TestSelector testSelector;

    private InputAmplDistributor inputAmplDistributor;

    private int globalNumberOfSelectedAmplification;

    private int numberOfIteration;

    public Amplification(DSpotCompiler compiler,
                         TestSelector testSelector,
                         InputAmplDistributor inputAmplDistributor,
                         int numberOfIteration) {
        this.compiler = compiler;
        this.assertionGenerator = new AssertionGenerator(this.compiler);
        this.globalNumberOfSelectedAmplification = 0;
        this.testSelector = testSelector;
        this.inputAmplDistributor = inputAmplDistributor;
        this.globalNumberOfSelectedAmplification = 0;
        this.numberOfIteration = numberOfIteration;
    }


    public List<CtMethod<?>>  amplification(CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified) {
        if(testMethodsToBeAmplified.isEmpty()) {
            LOGGER.warn("No test provided for amplification in class {}", testClassToBeAmplified.getQualifiedName());
            return Collections.emptyList();
        }

        LOGGER.info("Amplification of {} ({} test(s))", testClassToBeAmplified.getQualifiedName(), testMethodsToBeAmplified.size());
        LOGGER.info("Assertion amplification of {} ({} test(s))", testClassToBeAmplified.getQualifiedName(), testMethodsToBeAmplified.size());

        // here, we base the execution mode to the first test method given.
        // the user should provide whether JUnit3/4 OR JUnit5 but not both at the same time.
        // TODO DSpot could be able to switch from one to another version of JUnit, but I believe that the ROI is not worth it.
        final boolean jUnit5 = TestFramework.isJUnit5(testMethodsToBeAmplified.get(0));
        EntryPoint.jUnit5Mode = jUnit5;
        InputConfiguration.get().setJUnit5(jUnit5);
        if (!this.testSelector.init()) {
            return Collections.emptyList();
        }
        final List<CtMethod<?>> passingTests;
        try {
            passingTests =
                    TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                            testClassToBeAmplified,
                            testMethodsToBeAmplified,
                            this.compiler
                    );
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_EXEC_TEST_BEFORE_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        final List<CtMethod<?>> selectedToBeAmplified;
        try {
            // set up the selector with tests to amplify
            selectedToBeAmplified = this.testSelector.selectToAmplify(testClassToBeAmplified, passingTests);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_PRE_SELECTION, e));
            return Collections.emptyList();
        }

        // generate tests with additional assertions
        final List<CtMethod<?>> assertionAmplifiedTestMethods = this.assertionsAmplification(testClassToBeAmplified, selectedToBeAmplified);
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            // keep tests that improve the test suite
            amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(assertionAmplifiedTestMethods);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            return Collections.emptyList();
        }
        this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
        LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsToKeep.size(), this.globalNumberOfSelectedAmplification);
        // in case there is no amplifier, we can leave
        if (!this.inputAmplDistributor.shouldBeRun()) {
            return amplifiedTestMethodsToKeep;
        }

        // generate tests with input modification and associated new assertions
        LOGGER.info("Applying Input-amplification and Assertion-amplification test by test.");
        final List<CtMethod<?>> amplifiedTests = new ArrayList<>();
        for (int i = 0; i < testMethodsToBeAmplified.size(); i++) {
            CtMethod test = testMethodsToBeAmplified.get(i);
            LOGGER.info("Amplification of {}, ({}/{})", test.getSimpleName(), i + 1, testMethodsToBeAmplified.size());
            amplifiedTests.addAll(amplificationIteration(testClassToBeAmplified, test));
            this.globalNumberOfSelectedAmplification += amplifiedTests.size();
            LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTests.size(), this.globalNumberOfSelectedAmplification);
        }
        return amplifiedTests;
    }

    private List<CtMethod<?>>  amplificationIteration(CtType<?> testClassToBeAmplified, CtMethod test) {
        // tmp list for current test methods to be amplified
        // this list must be a implementation that support remove / clear methods
        List<CtMethod<?>> currentTestList = new ArrayList<>();
        currentTestList.add(test);
        // output
        final List<CtMethod<?>> amplifiedTests = new ArrayList<>();
        for (int i = 0; i < this.numberOfIteration ; i++) {
            LOGGER.info("iteration {} / {}", i, this.numberOfIteration);
            currentTestList = this.amplification(testClassToBeAmplified, currentTestList, amplifiedTests, i);
        }
        return amplifiedTests;
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
    protected List<CtMethod<?>> amplification(CtType<?> classTest, CtMethod test, int maxIteration) {
        // tmp list for current test methods to be amplified
        // this list must be a implementation that support remove / clear methods
        List<CtMethod<?>> currentTestList = new ArrayList<>();
        currentTestList.add(test);
        // output
        final List<CtMethod<?>> amplifiedTests = new ArrayList<>();
        for (int i = 0; i < maxIteration; i++) {
            LOGGER.info("iteration {} / {}", i, maxIteration);
            final List<CtMethod<?>> selectedToBeAmplified;
            try {

                // set up the selector with tests to amplify
                selectedToBeAmplified = this.testSelector.selectToAmplify(classTest, currentTestList);
            } catch (Exception | java.lang.Error e) {
//                Main.GLOBAL_REPORT.addError(new Error(ERROR_PRE_SELECTION, e));
                return Collections.emptyList();
            }
            if (selectedToBeAmplified.isEmpty()) {
                LOGGER.warn("No test could be selected to be amplified.");
                continue; // todo should we break the loop?
            }
            LOGGER.info("{} tests selected to be amplified over {} available tests",
                    selectedToBeAmplified.size(),
                    currentTestList.size()
            );
            final List<CtMethod<?>> inputAmplifiedTests;
            try {
                // amplify tests and shrink amplified set with budgetizer
                inputAmplifiedTests = this.budgetizer.inputAmplify(selectedToBeAmplified, i);
            } catch (Exception | java.lang.Error e) {
//                Main.GLOBAL_REPORT.addError(new Error(ERROR_INPUT_AMPLIFICATION, e));
                return Collections.emptyList();
            }

            // add assertions to input modified tests
            final List<CtMethod<?>> testsWithAssertions = this.assertionsAmplification(classTest, inputAmplifiedTests);

            // in case no test with assertions could be generated, we go for the next iteration.
            if (testsWithAssertions.isEmpty()) {
                currentTestList = inputAmplifiedTests;
                continue;
            }
            final List<CtMethod<?>> amplifiedTestMethodsToKeep;
            try {

                // keep tests that improve the test suite
                amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(testsWithAssertions);
            } catch (Exception | java.lang.Error e) {
//                Main.GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
                return Collections.emptyList();
            }
            amplifiedTests.addAll(amplifiedTestMethodsToKeep);
            LOGGER.info("{} amplified test methods has been selected to be kept.", amplifiedTestMethodsToKeep.size());

            // new amplified tests will be the basis for further amplification
            currentTestList = testsWithAssertions;
        }
        if (selectedToBeAmplified.isEmpty()) {
            LOGGER.warn("No test could be selected to be amplified.");
            return selectedToBeAmplified; // todo should we break the loop?
        }
        LOGGER.info("{} tests selected to be amplified over {} available tests",
                selectedToBeAmplified.size(),
                currentTestListToBeAmplified.size()
        );
        final List<CtMethod<?>> inputAmplifiedTests;
        try {
            // amplify tests and shrink amplified set with inputAmplDistributor
            inputAmplifiedTests = this.inputAmplDistributor.inputAmplify(selectedToBeAmplified, currentIteration);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_INPUT_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        // add assertions to input modified tests and return them
        // new amplified tests will be the basis for further amplification
        final List<CtMethod<?>> currentTestList = this.assertionsAmplification(testClassToBeAmplified, inputAmplifiedTests);

        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            // keep tests that improve the test suite
            amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(currentTestList);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            return Collections.emptyList();
        }
        LOGGER.info("{} amplified test methods has been selected to be kept.", amplifiedTestMethodsToKeep.size());
        amplifiedTests.addAll(amplifiedTestMethodsToKeep);
        return currentTestList;
    }

    public List<CtMethod<?>> assertionsAmplification(CtType<?> classTest, List<CtMethod<?>> testMethods) {
        final List<CtMethod<?>> testsWithAssertions;
        try {
            testsWithAssertions = this.assertionGenerator.assertionAmplification(classTest, testMethods);
        } catch (Exception | java.lang.Error e) {
//            Main.GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        if (testsWithAssertions.isEmpty()) {
            return testsWithAssertions;
        }
        // final check on A-amplified test, see if they all pass.
        // If they don't, we just discard them.
        final List<CtMethod<?>> amplifiedPassingTests =
                TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                        classTest,
                        testsWithAssertions,
                        this.compiler
                );
        LOGGER.info("Assertion amplification: {} test method(s) has been successfully amplified.", amplifiedPassingTests.size());
        return amplifiedPassingTests;
    }
}
