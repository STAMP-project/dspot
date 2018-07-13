package eu.stamp_project.dspot;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertGenerator.AssertGenerator;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
        final List<CtMethod<?>> selectedToBeAmplified = this.testSelector.selectToAmplify(passingTests);
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
            //compileAndRunTests(classTest, Collections.singletonList(tests.get(i)));
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
            final List<CtMethod<?>> selectedToBeAmplified = testSelector.selectToAmplify(currentTestList);
            if (selectedToBeAmplified.isEmpty()) {
                LOGGER.warn("No test could be selected to be amplified.");
                continue; // todo should we break the loop?
            }
            LOGGER.info("{} tests selected to be amplified over {} available tests",
                    selectedToBeAmplified.size(),
                    currentTestList.size()
            );
            final List<CtMethod<?>> inputAmplifiedTests = this.inputAmplifyTests(selectedToBeAmplified);
            final List<CtMethod<?>> reducedInputAmplifiedTests = AmplificationHelper.reduce(inputAmplifiedTests, this.configuration);
            final List<CtMethod<?>> testsWithAssertions = this.assertionsAmplification(classTest, reducedInputAmplifiedTests);
            // in case no test with assertions could be generated, we go for the next iteration.
            if (testsWithAssertions.isEmpty()) {
                currentTestList = reducedInputAmplifiedTests;
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
        final List<CtMethod<?>> amplifiedPassingTests = TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(classTest, testsWithAssertions, this.compiler, this.configuration);
        LOGGER.info("Assertion amplification: {} test method(s) has been successfully amplified.", testMethods.size());
        return amplifiedPassingTests;
    }

    /**
     * Input amplification of multiple tests.
     *
     * @param tests Test methods
     * @return New generated tests
     */
    private List<CtMethod<?>> inputAmplifyTests(List<CtMethod<?>> tests) {
        LOGGER.info("Amplification of inputs...");
        List<CtMethod<?>> amplifiedTests = tests.parallelStream()
                .flatMap(test -> {
                    DSpotUtils.printProgress(tests.indexOf(test), tests.size());
                    return inputAmplifyTest(test);
                }).collect(Collectors.toList());
        LOGGER.info("{} new tests generated", amplifiedTests.size());
        return amplifiedTests;
    }

    @Deprecated // this take too much time... We will filter before it: i.e. avoid redundant amplification.
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Input amplification for a single test.
     *
     * @param test Test method
     * @return New generated tests
     */
    private Stream<CtMethod<?>> inputAmplifyTest(CtMethod<?> test) {
        final CtMethod topParent = AmplificationHelper.getTopParent(test);
        return this.amplifiers.parallelStream()
                .flatMap(amplifier -> amplifier.apply(test))
                .filter(amplifiedTest -> amplifiedTest != null && !amplifiedTest.getBody().getStatements().isEmpty())
//                .filter(distinctByKey(CtMethod::getBody))
                .map(amplifiedTest ->
                        AmplificationHelper.addOriginInComment(amplifiedTest, topParent)
                );
    }

    private void resetAmplifiers(CtType parentClass) {
        this.amplifiers.forEach(amp -> amp.reset(parentClass));
    }

}
