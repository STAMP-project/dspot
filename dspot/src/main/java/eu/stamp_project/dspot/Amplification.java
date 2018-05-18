package eu.stamp_project.dspot;

import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertGenerator.AssertGenerator;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    public static int ampTestCount;

    public Amplification(InputConfiguration configuration, List<Amplifier> amplifiers, TestSelector testSelector, DSpotCompiler compiler) {
        this.configuration = configuration;
        this.amplifiers = amplifiers;
        this.testSelector = testSelector;
        this.compiler = compiler;
        this.assertGenerator = new AssertGenerator(this.configuration, this.compiler);
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
     * @param methods      Methods to amplify
     * @param maxIteration Number of amplification iterations
     */
    public void amplification(CtType<?> classTest, List<CtMethod<?>> methods, int maxIteration) {
        List<CtMethod<?>> tests = methods.stream()
                .filter(AmplificationChecker::isTest)
                .collect(Collectors.toList());
        if (tests.isEmpty()) {
            LOGGER.warn("No test has been found into {}", classTest.getQualifiedName());
            return;
        }
        LOGGER.info("amplification of {} ({} test(s))", classTest.getQualifiedName(), tests.size());
        preAmplification(classTest, tests);
        LOGGER.info("{} amplified test(s) has been selected, global: {}", this.testSelector.getAmplifiedTestCases().size() - ampTestCount, this.testSelector.getAmplifiedTestCases().size());
        ampTestCount = this.testSelector.getAmplifiedTestCases().size();
        // in case there is no amplifier, we can leave
        if (this.amplifiers.isEmpty()) {
            return;
        }
        resetAmplifiers(classTest);
        for (int i = 0; i < tests.size(); i++) {
            CtMethod test = tests.get(i);
            LOGGER.info("amp {} ({}/{})", test.getSimpleName(), i + 1, tests.size());
            compileAndRunTests(classTest, Collections.singletonList(tests.get(i)));
            amplification(classTest, test, maxIteration);
            LOGGER.info("{} amplified test(s) has been selected, global: {}", this.testSelector.getAmplifiedTestCases().size() - ampTestCount, this.testSelector.getAmplifiedTestCases().size());
            ampTestCount = this.testSelector.getAmplifiedTestCases().size();

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
        List<CtMethod<?>> currentTestList = new ArrayList<>();
        currentTestList.add(test);

        List<CtMethod<?>> amplifiedTests = new ArrayList<>();

        for (int i = 0; i < maxIteration; i++) {
            LOGGER.info("iteration {}:", i);
            List<CtMethod<?>> testsToBeAmplified = testSelector.selectToAmplify(currentTestList);
            if (testsToBeAmplified.isEmpty()) {
                LOGGER.info("No test could be generated from selected test");
                continue;
            }
            LOGGER.info("{} tests selected to be amplified over {} available tests",
                    testsToBeAmplified.size(),
                    currentTestList.size()
            );
            currentTestList = AmplificationHelper.reduce(inputAmplifyTests(testsToBeAmplified));
            List<CtMethod<?>> testsWithAssertions = assertGenerator.assertionAmplification(classTest, currentTestList);
            if (testsWithAssertions.isEmpty()) {
                continue;
            } else {
                currentTestList = testsWithAssertions;
            }
            final TestListener result = compileAndRunTests(classTest, currentTestList);
            if (result == null) {
                continue;
            } else if (!result.getFailingTests().isEmpty()) {
                LOGGER.warn("Discarding failing test cases");
                final Set<String> failingTestCase =
                        result.getFailingTests()
                                .stream()
                                .map(failure -> failure.testCaseName)
                                .collect(Collectors.toSet());
                currentTestList = currentTestList.stream()
                        .filter(ctMethod -> !failingTestCase.contains(ctMethod.getSimpleName()))
                        .collect(Collectors.toList());
            }
            currentTestList = AmplificationHelper.getPassingTests(currentTestList, result);
            LOGGER.info("{} test method(s) has been successfully generated", currentTestList.size());
            amplifiedTests.addAll(testSelector.selectToKeep(currentTestList));
        }
        return amplifiedTests;
    }

    /**
     * Adds new assertions in multiple tests.
     * <p>
     * <p>Makes sure the test suite is valid before the assertion amplification {@link AssertGenerator#assertionAmplification(CtType, List)}.
     *
     * @param classTest Test class
     * @param tests     New test methods
     * @return Valid amplified tests
     */
    private List<CtMethod<?>> preAmplification(CtType classTest, List<CtMethod<?>> tests) {
        TestListener result = compileAndRunTests(classTest, tests);
        if (!result.getFailingTests().isEmpty()) {
            LOGGER.warn("{} tests failed before the amplifications", result.getFailingTests().size());
            LOGGER.warn("{}", result.getFailingTests().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(System.getProperty("line.separator")))
            );
            LOGGER.warn("Discarding following test cases for the amplification");
            result.getFailingTests()
                    .stream()
                    .map(failure -> failure.testCaseName)
                    .forEach(failure -> {
                        try {
                            CtMethod testToRemove = tests.stream()
                                    .filter(m -> failure.equals(m.getSimpleName()))
                                    .findFirst().get();
                            tests.remove(tests.indexOf(testToRemove));
                            LOGGER.warn("{}", testToRemove.getSimpleName());
                        } catch (Exception ignored) {
                            //ignored
                        }
                    });
            return preAmplification(classTest, tests);
        } else {
            LOGGER.info("Try to add assertions before amplification");
            final List<CtMethod<?>> amplifiedTestToBeKept = assertGenerator.assertionAmplification(
                    classTest, testSelector.selectToAmplify(tests));
            if (!amplifiedTestToBeKept.isEmpty()) {
                compileAndRunTests(classTest, amplifiedTestToBeKept);
                testSelector.selectToKeep(amplifiedTestToBeKept);
                return testSelector.getAmplifiedTestCases();
            } else {
                return amplifiedTestToBeKept;
            }
        }
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
        return amplifiers.parallelStream()
                .flatMap(amplifier -> amplifier.apply(test).stream())
                .filter(amplifiedTest -> amplifiedTest != null && !amplifiedTest.getBody().getStatements().isEmpty())
                .filter(distinctByKey(CtMethod::getBody))
                .map(amplifiedTest ->
                        AmplificationHelper.addOriginInComment(amplifiedTest, topParent)
                );
    }

    private void resetAmplifiers(CtType parentClass) {
        amplifiers.forEach(amp -> amp.reset(parentClass));
    }

    /**
     * Adds test methods to the test class and run them.
     *
     * @param classTest       Test class
     * @param currentTestList New test methods to run
     * @return Results of tests' run
     * @throws AmplificationException
     */
    private TestListener compileAndRunTests(CtType classTest, List<CtMethod<?>> currentTestList) {
        CtType amplifiedTestClass = AmplificationHelper.cloneTestClassAndAddGivenTest(classTest, currentTestList);
        try {
            return TestCompiler.compileAndRun(
                    amplifiedTestClass,
                    this.compiler,
                    currentTestList,
                    this.configuration
            );
        } catch (AmplificationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds test methods to the test class and run them. Does not allow partially failing test classes.
     *
     * @param classTest       Test class
     * @param currentTestList New test methods to run
     * @return Results of tests run or {@code null} if a test failed or could not be run (uncompilable)
     */
    public TestListener compileAndRunTestsNoFail(CtType classTest, List<CtMethod<?>> currentTestList)
            throws AmplificationException {
        final TestListener result = compileAndRunTests(classTest, currentTestList);
        final long numberOfSubClasses = classTest.getFactory().Class().getAll().stream()
                .filter(subClass -> classTest.getReference().equals(subClass.getSuperclass()))
                .count();
        if (!result.getFailingTests().isEmpty() ||
                (!classTest.getModifiers().contains(ModifierKind.ABSTRACT) &&
                        result.getRunningTests().size() != currentTestList.size()) ||
                (classTest.getModifiers().contains(ModifierKind.ABSTRACT) &&
                        result.getRunningTests().size() != (numberOfSubClasses * currentTestList.size()))) {
            throw new AmplificationException(result.getFailingTests().size() +
                    "/ " + result.getRunningTests().size() + " test cases failed!"
                    + AmplificationHelper.LINE_SEPARATOR +
                    result.getFailingTests()
                            .stream()
                            .map(Failure::toString)
                            .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
            );
        } else {
            return result;
        }
    }
}
