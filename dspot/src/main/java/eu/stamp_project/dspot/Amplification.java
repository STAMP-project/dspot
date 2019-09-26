package eu.stamp_project.dspot;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
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

    private TestSelector testSelector;

    private InputAmplDistributor inputAmplDistributor;

    public Amplification(DSpotCompiler compiler, TestSelector testSelector, InputAmplDistributor inputAmplDistributor) {
        this.compiler = compiler;
        this.assertionGenerator = new AssertionGenerator(InputConfiguration.get(), this.compiler);
        this.testSelector = testSelector;
        this.inputAmplDistributor = inputAmplDistributor;
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
                                           int i) {
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
            inputAmplifiedTests = this.inputAmplDistributor.inputAmplify(selectedToBeAmplified, i);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_INPUT_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        // add assertions to input modified tests
        final List<CtMethod<?>> testsWithAssertions = this.assertionsAmplification(testClassToBeAmplified, inputAmplifiedTests);

        // in case no test with assertions could be generated, we go for the next iteration.
        if (testsWithAssertions.isEmpty()) {
            return inputAmplifiedTests;
        }
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            // keep tests that improve the test suite
            amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(testsWithAssertions);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            return Collections.emptyList();
        }
        LOGGER.info("{} amplified test methods has been selected to be kept.", amplifiedTestMethodsToKeep.size());
        // new amplified tests will be the basis for further amplification
        return amplifiedTestMethodsToKeep;
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
                TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                        classTest,
                        testsWithAssertions,
                        this.compiler,
                        InputConfiguration.get()
                );
        LOGGER.info("Assertion amplification: {} test method(s) has been successfully amplified.", amplifiedPassingTests.size());
        return amplifiedPassingTests;
    }
}
