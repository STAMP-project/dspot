package eu.stamp_project.dspot;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.TestSelector;
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

    private TestSelector testSelector;

    private InputAmplDistributor inputAmplDistributor;

    private int globalNumberOfSelectedAmplification;

    private int numberOfIteration;

    private TestCompiler testCompiler;

    public Amplification(double delta,
                         DSpotCompiler compiler,
                         TestSelector testSelector,
                         InputAmplDistributor inputAmplDistributor,
                         int numberOfIteration,
                         TestCompiler testCompiler) {
        this.compiler = compiler;
        this.testCompiler = testCompiler;
        this.assertionGenerator = new AssertionGenerator(delta, this.compiler, this.testCompiler);
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

        if (!this.testSelector.init()) {
            return Collections.emptyList();
        }
        final List<CtMethod<?>> passingTests;
        try {
            passingTests =
                    this.testCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
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
        for (int i = 0; i < testMethodsToBeAmplified.size(); i++) {
            CtMethod test = testMethodsToBeAmplified.get(i);
            LOGGER.info("Amplification of {}, ({}/{})", test.getSimpleName(), i + 1, testMethodsToBeAmplified.size());
            final List<CtMethod<?>> amplifiedTestMethodsFromCurrentIteration = amplificationIteration(testClassToBeAmplified, test);
            amplifiedTestMethodsToKeep.addAll(amplifiedTestMethodsFromCurrentIteration);
            this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
            LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsFromCurrentIteration.size(), this.globalNumberOfSelectedAmplification);
        }
        return amplifiedTestMethodsToKeep;
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
    public List<CtMethod<?>> amplification(CtType<?> testClassToBeAmplified,
                                           List<CtMethod<?>> currentTestListToBeAmplified,
                                           List<CtMethod<?>> amplifiedTests,
                                           int currentIteration) {
        final List<CtMethod<?>> selectedToBeAmplified;
        try {
            // set up the selector with tests to amplify
            selectedToBeAmplified = this.testSelector.selectToAmplify(testClassToBeAmplified, currentTestListToBeAmplified);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_PRE_SELECTION, e));
            return Collections.emptyList();
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
            Main.GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        if (testsWithAssertions.isEmpty()) {
            return testsWithAssertions;
        }
        // final check on A-amplified test, see if they all pass.
        // If they don't, we just discard them.
        final List<CtMethod<?>> amplifiedPassingTests =
                this.testCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                        classTest,
                        testsWithAssertions,
                        this.compiler
                );
        LOGGER.info("Assertion amplification: {} test method(s) has been successfully amplified.", amplifiedPassingTests.size());
        return amplifiedPassingTests;
    }
}
