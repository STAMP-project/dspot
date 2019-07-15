package eu.stamp_project.dspot;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertgenerator.AssertionGenerator;
import eu.stamp_project.dspot.budget.Budgetizer;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.report.error.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static eu.stamp_project.utils.report.error.ErrorEnum.*;


/**
 * User: Simon
 * Date: 03/12/15
 * Time: 13:52
 */
public class Amplification {

    private static final Logger LOGGER = LoggerFactory.getLogger(Amplification.class);

    private AssertionGenerator assertionGenerator;

    private DSpotCompiler compiler;

    private List<Amplifier> amplifiers;

    private TestSelector testSelector;

    private int globalNumberOfSelectedAmplification;

    private Budgetizer budgetizer;

    public Amplification(DSpotCompiler compiler, List<Amplifier> amplifiers, TestSelector testSelector, Budgetizer budgetizer) {
        this.compiler = compiler;
        this.assertionGenerator = new AssertionGenerator(InputConfiguration.get(), this.compiler);
        this.globalNumberOfSelectedAmplification = 0;
        this.amplifiers = amplifiers;
        this.testSelector = testSelector;
        this.budgetizer = budgetizer;
    }

    /**
     * Amplification of every method of a test class.
     *
     * See {@link #amplification(CtType, CtMethod, int)} for the details of amplification.
     *
     * @param classTest    Test class
     * @param maxIteration Number of amplification iterations
     */
    public void amplification(CtType<?> classTest, int maxIteration) {
        amplification(classTest, TestFramework.getAllTest(classTest), maxIteration);
    }

    /**
     * Amplification of multiple methods.
     *
     * See {@link #amplification(CtType, CtMethod, int)} for the details of amplification.
     *
     * @param classTest    Test class
     * @param tests        Methods to amplify
     * @param maxIteration Number of amplification iterations
     */
    public void amplification(CtType<?> classTest, List<CtMethod<?>> tests, int maxIteration) {
        LOGGER.info("Amplification of {} ({} test(s))", classTest.getQualifiedName(), tests.size());
        LOGGER.info("Assertion amplification of {} ({} test(s))", classTest.getQualifiedName(), tests.size());

        // here, we base the execution mode to the first test method given.
        // the user should provide whether JUnit3/4 OR JUnit5 but not both at the same time.
        // TODO DSpot could be able to switch from one to another version of JUnit, but I believe that the ROI is not worth it.
        final boolean jUnit5 = TestFramework.isJUnit5(tests.get(0));
        EntryPoint.jUnit5Mode = jUnit5;
        InputConfiguration.get().setJUnit5(jUnit5);
        if (!this.testSelector.init()) {
            return;
        }
        final List<CtMethod<?>> passingTests;
        try {
        passingTests = TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(classTest, tests, this.compiler, InputConfiguration.get());
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_EXEC_TEST_BEFORE_AMPLIFICATION, e));
            return;
        }
        final List<CtMethod<?>> selectedToBeAmplified;
        try {

            // set up the selector with tests to amplify
            selectedToBeAmplified = this.testSelector.selectToAmplify(classTest, passingTests);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_PRE_SELECTION, e));
            return;
        }

        // generate tests with additional assertions
        final List<CtMethod<?>> assertionAmplifiedTestMethods = this.assertionsAmplification(classTest, selectedToBeAmplified);
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {

            // keep tests that improve the test suite
            amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(assertionAmplifiedTestMethods);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            return;
        }
        this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
        LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsToKeep.size(), this.globalNumberOfSelectedAmplification);
        // in case there is no amplifier, we can leave
        if (this.amplifiers.isEmpty()) {
            return;
        }

        // generate tests with input modification and associated new assertions
        LOGGER.info("Applying Input-amplification and Assertion-amplification test by test.");
        this.resetAmplifiers(classTest);
        for (int i = 0; i < tests.size(); i++) {
            CtMethod test = tests.get(i);
            LOGGER.info("Amplification of {}, ({}/{})", test.getSimpleName(), i + 1, tests.size());
            final List<CtMethod<?>> amplifiedTestMethods = amplification(classTest, test, maxIteration);
            this.globalNumberOfSelectedAmplification += amplifiedTestMethods.size();
            LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsToKeep.size(), this.globalNumberOfSelectedAmplification);
        }
    }

    /**
     * Amplification of a single test.
     *
     * DSpot combines the different kinds of I-Amplification iteratively: at each iteration all kinds of
     * I-Amplification are applied, resulting in new tests. From one iteration to another, DSpot reuses the
     * previously amplified tests, and further applies I-Amplification.
     *
     * @param classTest    Test class
     * @param test         Method to amplify
     * @param maxIteration Number of amplification iterations
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
                Main.GLOBAL_REPORT.addError(new Error(ERROR_PRE_SELECTION, e));
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
                Main.GLOBAL_REPORT.addError(new Error(ERROR_INPUT_AMPLIFICATION, e));
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
                Main.GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
                return Collections.emptyList();
            }
            amplifiedTests.addAll(amplifiedTestMethodsToKeep);
            LOGGER.info("{} amplified test methods has been selected to be kept.", amplifiedTestMethodsToKeep.size());

            // new amplified tests will be the basis for further amplification
            currentTestList = testsWithAssertions;
        }
        return amplifiedTests;
    }

    protected List<CtMethod<?>> assertionsAmplification(CtType<?> classTest, List<CtMethod<?>> testMethods) {
        final List<CtMethod<?>> testsWithAssertions;
        try {
            testsWithAssertions = this.assertionGenerator.assertionAmplification(classTest, testMethods);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
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
                        this.compiler,
                        InputConfiguration.get()
                );
        LOGGER.info("Assertion amplification: {} test method(s) has been successfully amplified.", amplifiedPassingTests.size());
        return amplifiedPassingTests;
    }


    private void resetAmplifiers(CtType parentClass) {
        this.amplifiers.forEach(amp -> amp.reset(parentClass));
    }

}
