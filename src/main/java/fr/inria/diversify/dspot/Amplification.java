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
//    private Map<Boolean, List<CtMethod>> testsStatus;//should not be there

    public Amplification(InputProgram inputProgram, DSpotCompiler compiler, DiversifyClassLoader applicationClassLoader, List<Amplifier> amplifiers, File logDir) {
        this.inputProgram = inputProgram;
        this.compiler = compiler;
        this.applicationClassLoader = applicationClassLoader;
        this.amplifiers = amplifiers;
        this.logDir = logDir;
        classWithLoggerBuilder = new ClassWithLoggerBuilder(inputProgram);
        testSelector = new TestSelector(logDir, 10);
        this.testStatus = new TestStatus();
    }

    public List<CtMethod> amplification(CtType classTest, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        return amplification(classTest, getAllTest(classTest), maxIteration);
    }

    public List<CtMethod> amplification(CtType classTest, List<CtMethod> methods, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod> tests = methods.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, inputProgram.getRelativeTestSourceCodeDir()))
                .collect(Collectors.toList());

        if(tests.isEmpty()) {
            return null;
        }
        testSelector.init();
        CtType classWithLogger = classWithLoggerBuilder.buildClassWithLogger(classTest, tests);
        boolean status = writeAndCompile(classWithLogger);
        if(!status) {
            Log.info("error with Logger in class {}", classTest);
            return null;
        }
        JunitResult result = runTests(classWithLogger, tests);
        testSelector.updateLogInfo();
//        LogResult.addCoverage(testSelector.getCoverage(), tests, true);
        resetAmplifiers(classTest, testSelector.getGlobalCoverage());

        Log.info("amplification of {} ({} test)", classTest.getQualifiedName(), tests.size());

        List<CtMethod> ampTest = new ArrayList<>();
        for(int i = 0; i < tests.size(); i++) {
            CtMethod test = tests.get(i);
            Log.debug("amp {} ({}/{})", test.getSimpleName(), i+1, tests.size());
            testSelector.init();

            classWithLogger = classWithLoggerBuilder.buildClassWithLogger(classTest, tests.get(i));
            writeAndCompile(classWithLogger);

            result = runTest(classWithLogger, test);
            if(result != null
                    && result.getFailures().isEmpty()
                    && !result.getTestRuns().isEmpty()) {
                testSelector.updateLogInfo();

                amplification(classTest, test, maxIteration);

                Set<CtMethod> selectedAmpTests = new HashSet<>();
                selectedAmpTests.addAll(testSelector.selectedAmplifiedTests(testStatus.get(false)));
                selectedAmpTests.addAll(testSelector.selectedAmplifiedTests(testStatus.get(true)));
                ampTest.addAll(selectedAmpTests);
                ampTestCount += ampTest.size();
                Log.debug("total amp test: {}, global: {}", ampTest.size(), ampTestCount);
//                LogResult.addCoverage(testSelector.getCoverage(), selectedAmpTests, false);
            }
        }
        return ampTest;
    }

    private void amplification(CtType originalClass, CtMethod test, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        testStatus.reset();
        List<CtMethod> newTests = new ArrayList<>();
        newTests.add(test);

        /* should not be there */
        Collection<CtMethod> ampTests = new ArrayList<>();
        ampTests.add(test);

        for (int i = 0; i < maxIteration; i++) {
            Log.debug("iteration {}:", i);

            Collection<CtMethod> testToAmp = testSelector.selectTestToAmp(ampTests, newTests);
            if(testToAmp.isEmpty()) {
                continue;
            }
            Log.debug("{} tests selected to be amplified", testToAmp.size());
            newTests = ampTests(testToAmp);

            if (newTests.isEmpty()) {
                continue;
            }

            Log.debug("{} new tests generated", newTests.size());

            newTests = reduce(newTests);

            CtType classWithLogger = classWithLoggerBuilder.buildClassWithLogger(originalClass, newTests);
            boolean status = writeAndCompile(classWithLogger);
            if(!status) {
                break;
            }
            Log.debug("run tests");
            JunitResult result = runTests(classWithLogger, newTests);

            if(result == null) {
                continue;
            }
            newTests = AmplificationHelper.filterTest(newTests, result);
            ampTests.addAll(newTests);
            testStatus.updateTestStatus(newTests, result);
            Log.debug("update coverage info");
            testSelector.updateLogInfo();
        }
    }

    private List<CtMethod> ampTests(Collection<CtMethod> tests) {
        return tests.stream()
                .flatMap(test -> ampTest(test).stream())
                 .filter(test -> test != null && !test.getBody().getStatements().isEmpty())
                .collect(Collectors.toList());
    }

    private List<CtMethod> ampTest(CtMethod test) {
        return amplifiers.stream()
                .flatMap(amplifier -> amplifier.apply(test).stream())
                .collect(Collectors.toList());
    }

    private List<CtMethod> reduce(List<CtMethod> newTests) {
        while(newTests.size() > 6000) {
            newTests.remove(AmplificationHelper.getRandom().nextInt(newTests.size()));
        }
        return newTests;
    }

    private void resetAmplifiers(CtType parentClass, Coverage coverage) {
        amplifiers.forEach(amp -> amp.reset(coverage, parentClass));
    }

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