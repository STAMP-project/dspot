package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.amp.*;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.logger.Logger;
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

    private DiversifyClassLoader applicationClassLoader;
    private InputProgram inputProgram;
    private File logDir;
    private List<Amplifier> amplifiers;
    private DSpotCompiler compiler;
    private TestSelector testSelector;
    private ClassWithLoggerBuilder classWithLoggerBuilder;

    private static int ampTestCount;

    private TestStatus testStatus;

    public Amplification(InputProgram inputProgram, DSpotCompiler compiler, DiversifyClassLoader applicationClassLoader, List<Amplifier> amplifiers, File logDir) {
        this.inputProgram = inputProgram;
        this.compiler = compiler;
        this.applicationClassLoader = applicationClassLoader;
        this.amplifiers = amplifiers;
        this.logDir = logDir;
        this.classWithLoggerBuilder = new ClassWithLoggerBuilder(inputProgram);
        this.testSelector = new TestSelector(logDir, 10);
        this.testStatus = new TestStatus();
    }

    public List<CtMethod> amplification(CtType classTest, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        return amplification(classTest, getAllTest(classTest), maxIteration);
    }

    public List<CtMethod> amplification(CtType classTest, Set<CtMethod> methods, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod> tests = methods.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, inputProgram.getRelativeTestSourceCodeDir()))
                .collect(Collectors.toList());

        if (tests.isEmpty()) {
            return null;
        }
        testSelector.init();
        CtType classWithLogger = classWithLoggerBuilder.buildClassWithLogger(classTest, tests);
        boolean status = writeAndCompile(classWithLogger);
        if (!status) {
            Log.info("error with Logger in class {}", classTest);
            return null;
        }
        runTests(classWithLogger, tests);
        testSelector.update();
        resetAmplifiers(classTest, testSelector.getGlobalCoverage());

        Log.info("amplification of {} ({} test)", classTest.getQualifiedName(), tests.size());

        List<CtMethod> ampTest = new ArrayList<>();
        for (int i = 0; i < tests.size(); i++) {
            CtMethod test = tests.get(i);
            Log.debug("amp {} ({}/{})", test.getSimpleName(), i + 1, tests.size());
            testSelector.init();

            classWithLogger = classWithLoggerBuilder.buildClassWithLogger(classTest, tests.get(i));
            writeAndCompile(classWithLogger);

            JunitResult result = runTest(classWithLogger, test);
            if (result != null
                    && result.getFailures().isEmpty()
                    && !result.getTestRuns().isEmpty()) {
                testSelector.update();

                amplification(classTest, test, maxIteration);

                Set<CtMethod> selectedAmpTests = new HashSet<>();
                selectedAmpTests.addAll(testSelector.selectedAmplifiedTests(testStatus.get(false)));
                selectedAmpTests.addAll(testSelector.selectedAmplifiedTests(testStatus.get(true)));
                ampTest.addAll(selectedAmpTests);
                ampTestCount += ampTest.size();
                Log.debug("total amp test: {}, global: {}", ampTest.size(), ampTestCount);
            }
        }
        return ampTest;
    }

    private void amplification(CtType originalClass, CtMethod test, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        testStatus.reset();
        List<CtMethod> amplifiedTests = new ArrayList<>();
        amplifiedTests.add(test);

        /* should not be there */
        Collection<CtMethod> ampTests = new ArrayList<>();
        ampTests.add(test);

        for (int i = 0; i < maxIteration; i++) {
            Log.debug("iteration {}:", i);

            Collection<CtMethod> testToBeAmplify = testSelector.selectTests(ampTests, amplifiedTests);
            if (testToBeAmplify.isEmpty()) {
                continue;
            }
            Log.debug("{} tests selected to be amplified over {} available tests", testToBeAmplify.size(), amplifiedTests.size());
            amplifiedTests = amplifyTests(testToBeAmplify);

            if (amplifiedTests.isEmpty()) {
                Log.debug("No test could be generated from selected test");
                continue;
            }

            Log.debug("{} new tests generated", amplifiedTests.size());
            amplifiedTests = reduce(amplifiedTests);
            CtType classWithLogger = classWithLoggerBuilder.buildClassWithLogger(originalClass, amplifiedTests);
            boolean status = writeAndCompile(classWithLogger);
            if (!status) {
                break;
            }
            Log.debug("run tests");
            JunitResult result = runTests(classWithLogger, amplifiedTests);
            amplifiedTests = AmplificationHelper.filterTest(amplifiedTests, result);

            Log.debug("update test status");
            testStatus.updateTestStatus(amplifiedTests, result);
            Log.debug("update coverage info");
            testSelector.update();
        }
    }

    private List<CtMethod> amplifyTests(Collection<CtMethod> tests) {
        return tests.stream()
                .flatMap(test -> amplifyTest(test).stream())
                .filter(test -> test != null && !test.getBody().getStatements().isEmpty())
                .collect(Collectors.toList());
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

    private void resetAmplifiers(CtType parentClass, Coverage coverage) {
        amplifiers.forEach(amp -> amp.reset(coverage, parentClass));
    }

    /*
        TODO TO BE MOVED
     */
    private Set<CtMethod> getAllTest(CtType classTest) {
        Set<CtMethod> mths = classTest.getMethods();
        return mths.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, inputProgram.getRelativeTestSourceCodeDir()))
                .distinct()
                .collect(Collectors.toSet());
    }

    private JunitResult runTest(CtType testClass, CtMethod test) throws ClassNotFoundException {
        return runTests(testClass, Collections.singletonList(test));
    }

    private JunitResult runTests(CtType testClass, Collection<CtMethod> tests) throws ClassNotFoundException {
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
        return (new TestRunner(inputProgram, applicationClassLoader, compiler)).writeAndCompile(classInstru);
    }

}