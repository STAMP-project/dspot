package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
<<<<<<< 02d09f7a66262901a5d2dd7ca279bc2c62bd9614
import fr.inria.diversify.dspot.amp.*;
import fr.inria.diversify.dspot.assertGenerator.AssertGenerator;
=======
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.selector.BranchCoverageTestSelector;
>>>>>>> refactor amplifier package
import fr.inria.diversify.dspot.support.DSpotCompiler;
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
//    private TestSelector testSelector;
    private fr.inria.diversify.dspot.selector.TestSelector testSelector;
    private ClassWithLoggerBuilder classWithLoggerBuilder;
    private AssertGenerator assertGenerator;
    private TestStatus testStatus;

    private static int ampTestCount;

    public Amplification(InputProgram inputProgram, DSpotCompiler compiler, DiversifyClassLoader applicationClassLoader, List<Amplifier> amplifiers, File logDir) {
        this.inputProgram = inputProgram;
        this.compiler = compiler;
        this.applicationClassLoader = applicationClassLoader;
        this.amplifiers = amplifiers;
        this.logDir = logDir;
        this.classWithLoggerBuilder = new ClassWithLoggerBuilder(inputProgram);
//        this.testSelector = new TestSelector(logDir, 10);
        this.testSelector = new BranchCoverageTestSelector(logDir, 10);
        this.testStatus = new TestStatus();
        this.assertGenerator = new AssertGenerator(inputProgram, compiler, applicationClassLoader);
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
        testSelector.init();
        CtType classWithLogger = classWithLoggerBuilder.buildClassWithLogger(classTest, tests);
        boolean status = writeAndCompile(classWithLogger);
        if (!status) {
            Log.info("error with Logger in class {}", classTest);
            return null;
        }

        runTests(classWithLogger, tests);
        testSelector.update();
        resetAmplifiers(classTest);

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

                List<CtMethod> amplification = amplification(classTest, test, maxIteration);
                ampTest.addAll(amplification);
                ampTestCount += amplification.size();
                Log.debug("total amp test: {}, global: {}", amplification.size(), ampTestCount);
            }
        }
        return AmplificationHelper.addAmplifiedTestToClass(ampTest, classTest);
    }


    private List<CtMethod> amplification(CtType classTest, CtMethod test, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        testStatus.reset();
        List<CtMethod> newTests = new ArrayList<>();
        newTests.add(test);

        Collection<CtMethod> ampTests = new ArrayList<>();
        ampTests.add(test);

        List<CtMethod> amplifiedTests = new ArrayList<>();

        for (int i = 0; i < maxIteration; i++) {
            Log.debug("iteration {}:", i);

            List<CtMethod> testToBeAmplified = testSelector.selectTestToBeAmplified(ampTests, newTests);
            if (testToBeAmplified.isEmpty()) {
                Log.debug("No test could be generated from selected test");
                continue;
            }
            Log.debug("{} tests selected to be amplified over {} available tests", testToBeAmplified.size(), newTests.size());

            newTests = reduce(amplifyTests(testToBeAmplified));

            List<CtMethod> testWithAssertions = assertGenerator.generateAsserts(classTest, newTests, AmplificationHelper.getAmpTestToParent());
            if (testWithAssertions.isEmpty()) {
                continue;
            } else {
                newTests = testWithAssertions;
            }

            CtType classWithLogger = classWithLoggerBuilder.buildClassWithLogger(classTest, newTests);
            boolean status = writeAndCompile(classWithLogger);
            if (!status) {
                break;
            }

            JunitResult result = runTests(classWithLogger, newTests);
            if (result == null) {
                continue;
            }

            testStatus.updateTestStatus(newTests, result);
            Log.debug("update coverage info");
            testSelector.update();

            newTests = AmplificationHelper.filterTest(newTests, result);
            Log.debug("{} test method(s) has been successfully generated", newTests.size());
            amplifiedTests.addAll(testSelector.selectTestAmongAmplifiedTests(newTests));
            ampTests.addAll(newTests);
        }
        return amplifiedTests;
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

    private JunitResult runTest(CtType testClass, CtMethod test) throws ClassNotFoundException {
        return runTests(testClass, Collections.singletonList(test));
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
        return (new TestRunner(inputProgram, applicationClassLoader, compiler)).writeAndCompile(classInstru);
    }

}