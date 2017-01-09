package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.assertGenerator.AssertGenerator;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.JunitRunner;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.testRunner.TestStatus;
import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 03/12/15
 * Time: 13:52
 */
public class Amplification {

    private InputProgram inputProgram;
    private InputConfiguration inputConfiguration;
    private DiversifyClassLoader applicationClassLoader;
    private File logDir;
    private List<Amplifier> amplifiers;
    private DSpotCompiler compiler;
    private TestSelector testSelector;
    private ClassWithLoggerBuilder classWithLoggerBuilder;
    private AssertGenerator assertGenerator;
    private TestStatus testStatus;

    private static int ampTestCount;

    public Amplification(InputProgram inputProgram, InputConfiguration inputConfiguration, DSpotCompiler compiler, DiversifyClassLoader applicationClassLoader, List<Amplifier> amplifiers, TestSelector testSelector, File logDir) {
        this.inputProgram = inputProgram;
        this.compiler = compiler;
        this.applicationClassLoader = applicationClassLoader;
        this.amplifiers = amplifiers;
        this.logDir = logDir;
        this.classWithLoggerBuilder = new ClassWithLoggerBuilder(inputProgram);
        this.testSelector = testSelector;
        this.testStatus = new TestStatus();
        this.assertGenerator = new AssertGenerator(inputProgram, compiler, applicationClassLoader);
        this.inputConfiguration = inputConfiguration;
    }

    public CtType amplification(CtType classTest, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        return amplification(classTest, getAllTest(classTest), maxIteration);
    }

    public CtType amplification(CtType classTest, List<CtMethod> methods, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod> tests = methods.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, inputProgram.getRelativeTestSourceCodeDir()))
                .collect(Collectors.toList());
        if (tests.isEmpty()) {
            return null;
        }
        Log.info("amplification of {} ({} test)", classTest.getQualifiedName(), tests.size());
        testSelector.reset();
        List<CtMethod> ampTest = new ArrayList<>();

        List<CtMethod> preAmplification = preAmplification(classTest, tests);
        updateAmplifiedTestList(ampTest, preAmplification);

        for (int i = 0; i < tests.size(); i++) {
            CtMethod test = tests.get(i);
            Log.debug("amp {} ({}/{})", test.getSimpleName(), i + 1, tests.size());
            testSelector.reset();
            JunitResult result = compileAndRunTests(classTest, Collections.singletonList(tests.get(i)));
            if (result != null
                    && result.getFailures().isEmpty()
                    && !result.getTestRuns().isEmpty()) {
                List<CtMethod> amplification = amplification(classTest, test, maxIteration);
                updateAmplifiedTestList(ampTest, amplification);
            }
        }
        return AmplificationHelper.addAmplifiedTestToClass(ampTest, classTest);
    }

    private List<CtMethod> amplification(CtType classTest, CtMethod test, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        testStatus.reset();
        List<CtMethod> currentTestList = new ArrayList<>();
        currentTestList.add(test);

        List<CtMethod> amplifiedTests = new ArrayList<>();

        for (int i = 0; i < maxIteration; i++) {
            Log.debug("iteration {}:", i);

            List<CtMethod> testToBeAmplified = testSelector.selectToAmplify(currentTestList);
            if (testToBeAmplified.isEmpty()) {
                Log.debug("No test could be generated from selected test");
                continue;
            }
            Log.debug("{} tests selected to be amplified over {} available tests", testToBeAmplified.size(), currentTestList.size());

            currentTestList = reduce(amplifyTests(testToBeAmplified));

            List<CtMethod> testWithAssertions = assertGenerator.generateAsserts(classTest, currentTestList, AmplificationHelper.getAmpTestToParent());
            if (testWithAssertions.isEmpty()) {
                continue;
            } else {
                currentTestList = testWithAssertions;
            }
            JunitResult result = compileAndRunTests(classTest, currentTestList);
            testStatus.updateTestStatus(currentTestList, result);
            currentTestList = AmplificationHelper.filterTest(currentTestList, result);
            Log.debug("{} test method(s) has been successfully generated", currentTestList.size());
            amplifiedTests.addAll(testSelector.selectToKeep(currentTestList));
        }
        return amplifiedTests;
    }

    private void updateAmplifiedTestList(List<CtMethod> ampTest, List<CtMethod> amplification) {
        ampTest.addAll(amplification);
        ampTestCount += amplification.size();
        Log.debug("total amp test: {}, global: {}", amplification .size(), ampTestCount);
    }

    private List<CtMethod> preAmplification(CtType classTest, List<CtMethod> tests) throws IOException, ClassNotFoundException {
        compileAndRunTests(classTest, tests);
        testSelector.update();
        resetAmplifiers(classTest);
        Log.debug("Try to add assertions before amplification");
        List<CtMethod> preAmplifiedMethods = testSelector.selectToKeep(
                assertGenerator.generateAsserts(
                        classTest, testSelector.selectToAmplify(tests), AmplificationHelper.getAmpTestToParent()
                )
        );
        if (tests.containsAll(preAmplifiedMethods)) {
            return new ArrayList<>();
        } else {
            return preAmplifiedMethods;
        }
    }

    private List<CtMethod> amplifyTests(Collection<CtMethod> tests) {
        List<CtMethod> amplifiedTests = tests.stream()
                .flatMap(test -> amplifyTest(test).stream())
                .filter(test -> test != null && !test.getBody().getStatements().isEmpty())
                .collect(Collectors.toList());
        Log.debug("{} new tests generated", amplifiedTests.size());
        return amplifiedTests;
    }

    private List<CtMethod> amplifyTest(CtMethod test) {
        return amplifiers.stream()
                .flatMap(amplifier -> amplifier.apply(test).stream())
                .collect(Collectors.toList());
    }

    //empirically 200 seems to be enough
    private static final int MAX_NUMBER_OF_TESTS = 200;

    private List<CtMethod> reduce(List<CtMethod> newTests) {
        if (newTests.size() > MAX_NUMBER_OF_TESTS) {
            Log.warn("Too many tests has been generated: {}", newTests.size());
            Collections.shuffle(newTests, AmplificationHelper.getRandom());
            List<CtMethod> reducedNewTests = newTests.subList(0, MAX_NUMBER_OF_TESTS);
            Log.debug("Number of generated test reduced to {}", MAX_NUMBER_OF_TESTS);
            return reducedNewTests;
        } else {
            return newTests;
        }
    }

    private void resetAmplifiers(CtType parentClass) {
        amplifiers.forEach(amp -> amp.reset(parentClass));
    }

    private JunitResult compileAndRunTests(CtType classTest, List<CtMethod> currentTestList) {
        CtType classWithLogger = classWithLoggerBuilder.buildClassWithLogger(classTest, currentTestList);
        boolean status = writeAndCompile(classWithLogger);
        if (!status) {
            return null;
        }
        JunitResult result;
        try {
            result = runTests(classWithLogger, currentTestList);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
        if (result == null) {
            return null;
        }
        Log.debug("update test selector");
        testSelector.update();
        return result;
    }

    /*
        TODO TO BE MOVED
     */
    private List<CtMethod> getAllTest(CtType classTest) {
        Set<CtMethod> mths = classTest.getMethods();
        return mths.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, inputProgram.getRelativeTestSourceCodeDir()))
                .distinct()
                .collect(Collectors.toList());
    }

    private JunitResult runTests(CtType testClass, Collection<CtMethod> tests) throws ClassNotFoundException {
        Log.debug("run test class: {}", testClass.getQualifiedName());
        ClassLoader classLoader = new DiversifyClassLoader(applicationClassLoader, compiler.getBinaryOutputDirectory().getAbsolutePath());
        JunitRunner junitRunner = new JunitRunner(classLoader);
        Logger.reset();
        Logger.setLogDir(new File(logDir.getAbsolutePath()));
        String currentUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", inputProgram.getProgramDir());
        JunitResult result = junitRunner.runTestClass(testClass.getQualifiedName(), tests.stream()
                .map(test -> test.getSimpleName())
                .collect(Collectors.toList()));
        System.setProperty("user.dir", currentUserDir);
        return result;
    }

    private boolean writeAndCompile(CtType classInstru) {
        return (new TestRunner(applicationClassLoader, compiler)).writeAndCompile(classInstru);
    }

}