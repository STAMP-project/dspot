package eu.stamp_project.dspot;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertgenerator.AssertGenerator;
import eu.stamp_project.dspot.budget.Budgetizer;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * User: Simon
 * Date: 03/12/15
 * Time: 13:52
 */
public class Amplification {

    private static final Logger LOGGER = LoggerFactory.getLogger(Amplification.class);

    private InputConfiguration configuration;

    private List<Amplifier> amplifiers;

    private TestSelector testSelector;

    private AssertGenerator assertGenerator;

    private DSpotCompiler compiler;

    private int globalNumberOfSelectedAmplification;

    /**
     * @return the number of selected amplified test method. This number is counted over the whole run, <i>i.e.</i> potentially for multiple test classes.
     */
    public int getGlobalNumberOfSelectedAmplification() {
        return globalNumberOfSelectedAmplification;
    }

    public Amplification(InputConfiguration configuration, List<Amplifier> amplifiers, TestSelector testSelector, DSpotCompiler compiler) {
        this.configuration = configuration;
        this.amplifiers = amplifiers;
        this.testSelector = testSelector;
        this.compiler = compiler;
        this.assertGenerator = new AssertGenerator(this.configuration, this.compiler);
        this.globalNumberOfSelectedAmplification = 0;
    }

    /**
     * Amplification of every method of a test class.
     * <p>
     * <p>See {@link #amplification(CtType, CtMethod, int)} for the details of amplification.
     *
     * @param classTest    Test class
     * @param maxIteration Number of amplification iterations
     */
    public void amplification(CtType<?> classTest, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        amplification(classTest, AmplificationHelper.getAllTest(classTest), maxIteration);
    }

    /**
     * Amplification of multiple methods.
     * <p>
     * <p>See {@link #amplification(CtType, CtMethod, int)} for the details of amplification.
     *
     * @param classTest    Test class
     * @param tests        Methods to amplify
     * @param maxIteration Number of amplification iterations
     */
    public void amplification(CtType<?> classTest, List<CtMethod<?>> tests, int maxIteration) {
        LOGGER.info("Amplification of {} ({} test(s))", classTest.getQualifiedName(), tests.size());
        LOGGER.info("Assertion amplification of {} ({} test(s))", classTest.getQualifiedName(), tests.size());
        final List<CtMethod<?>> passingTests = TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(classTest, tests, this.compiler, this.configuration);
        final List<CtMethod<?>> selectedToBeAmplified = this.testSelector.selectToAmplify(classTest, passingTests);
        final List<CtMethod<?>> assertionAmplifiedTestMethods = this.assertionsAmplification(classTest, selectedToBeAmplified);
        final List<CtMethod<?>> amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(assertionAmplifiedTestMethods);
        this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
        LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsToKeep.size(), this.globalNumberOfSelectedAmplification);
        // in case there is no amplifier, we can leave
        if (this.amplifiers.isEmpty()) {
            return;
        }
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
     * <p>
     * <p>DSpot combines the different kinds of I-Amplification iteratively: at each iteration all kinds of
     * I-Amplification are applied, resulting in new tests. From one iteration to another, DSpot reuses the
     * previously amplified tests, and further applies I-Amplification.
     *
     * @param classTest    Test class
     * @param test         Method to amplify
     * @param maxIteration Number of amplification iterations
     * @return Valid amplified tests
     */
    private List<CtMethod<?>> amplification(CtType<?> classTest, CtMethod test, int maxIteration) {
        // tmp list for current test methods to be amplified
        // this list must be a implementation that support remove / clear methods
        List<CtMethod<?>> currentTestList = new ArrayList<>();
        currentTestList.add(test);
        // output
        final List<CtMethod<?>> amplifiedTests = new ArrayList<>();
        for (int i = 0; i < maxIteration; i++) {
            LOGGER.info("iteration {} / {}", i, maxIteration);
            final List<CtMethod<?>> selectedToBeAmplified = testSelector.selectToAmplify(classTest, currentTestList);
            if (selectedToBeAmplified.isEmpty()) {
                LOGGER.warn("No test could be selected to be amplified.");
                continue; // todo should we break the loop?
            }
            LOGGER.info("{} tests selected to be amplified over {} available tests",
                    selectedToBeAmplified.size(),
                    currentTestList.size()
            );
            final Budgetizer budgetizer = InputConfiguration.get().getBudgetizer();
            final List<CtMethod<?>> inputAmplifiedTests = budgetizer.inputAmplify(selectedToBeAmplified, i);
            final List<CtMethod<?>> testsWithAssertions = this.assertionsAmplification(classTest, inputAmplifiedTests);
            // in case no test with assertions could be generated, we go for the next iteration.
            if (testsWithAssertions.isEmpty()) {
                currentTestList = inputAmplifiedTests;
                continue;
            }
            final List<CtMethod<?>> amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(testsWithAssertions);
            amplifiedTests.addAll(amplifiedTestMethodsToKeep);
            LOGGER.info("{} amplified test methods has been selected to be kept.", amplifiedTestMethodsToKeep.size());
            currentTestList = testsWithAssertions;
        }
        return amplifiedTests;
    }

    private List<CtMethod<?>> assertionsAmplification(CtType<?> classTest, List<CtMethod<?>> testMethods) {
        List<CtMethod<?>> testsWithAssertions = this.assertGenerator.assertionAmplification(classTest, testMethods);
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
                        this.configuration
                );
        LOGGER.info("Assertion amplification: {} test method(s) has been successfully amplified.", amplifiedPassingTests.size());
        return amplifiedPassingTests;
    }


    private void resetAmplifiers(CtType parentClass) {
        this.amplifiers.forEach(amp -> amp.reset(parentClass));
    }

}
