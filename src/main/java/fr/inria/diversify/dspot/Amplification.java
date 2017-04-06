package fr.inria.diversify.dspot;

import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.assertGenerator.AssertGenerator;
import fr.inria.diversify.dspot.assertGenerator.AssertGeneratorHelper;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.TestCompiler;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 03/12/15
 * Time: 13:52
 */
public class Amplification {

    private InputProgram inputProgram;
    private List<Amplifier> amplifiers;
    private TestSelector testSelector;
    private AssertGenerator assertGenerator;
    private DSpotCompiler compiler;

    private static int ampTestCount;

    public Amplification(InputProgram inputProgram, List<Amplifier> amplifiers, TestSelector testSelector, DSpotCompiler compiler) {
        this.inputProgram = inputProgram;
        this.amplifiers = amplifiers;
        this.testSelector = testSelector;
        this.compiler = compiler;
        this.assertGenerator = new AssertGenerator(this.inputProgram, this.compiler);
    }

    public CtType amplification(CtType classTest, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        return amplification(classTest, AmplificationHelper.getAllTest(this.inputProgram, classTest), maxIteration);
    }

    public CtType amplification(CtType classTest, List<CtMethod<?>> methods, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod<?>> tests = methods.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, inputProgram.getRelativeTestSourceCodeDir()))
                .collect(Collectors.toList());
        if (tests.isEmpty()) {
            return null;
        }
        Log.info("amplification of {} ({} test)", classTest.getQualifiedName(), tests.size());
        testSelector.reset();
        List<CtMethod<?>> ampTest = new ArrayList<>();

        updateAmplifiedTestList(ampTest, preAmplification(classTest, tests));

        for (int i = 0; i < tests.size(); i++) {
            CtMethod test = tests.get(i);
            Log.debug("amp {} ({}/{})", test.getSimpleName(), i + 1, tests.size());
            testSelector.reset();
            JunitResult result = compileAndRunTests(classTest, Collections.singletonList(tests.get(i)));
            if (result != null) {
                if (result.getFailures().isEmpty()
                        && !result.getTestRuns().isEmpty()) {
                    updateAmplifiedTestList(ampTest,
                            amplification(classTest, test, maxIteration));
                } else {
                    Log.debug("{} / {} test cases failed!", result.getFailures().size(), result.getTestRuns().size());
                }
            }
        }
        return AmplificationHelper.createAmplifiedTest(ampTest, classTest);
    }

    private List<CtMethod<?>> amplification(CtType classTest, CtMethod test, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod<?>> currentTestList = new ArrayList<>();
        currentTestList.add(test);

        List<CtMethod<?>> amplifiedTests = new ArrayList<>();

        for (int i = 0; i < maxIteration; i++) {
            Log.debug("iteration {}:", i);

            List<CtMethod<?>> testToBeAmplified = testSelector.selectToAmplify(currentTestList);
            if (testToBeAmplified.isEmpty()) {
                Log.debug("No test could be generated from selected test");
                continue;
            }
            Log.debug("{} tests selected to be amplified over {} available tests", testToBeAmplified.size(), currentTestList.size());

            currentTestList = AmplificationHelper.reduce(amplifyTests(testToBeAmplified));

            List<CtMethod<?>> testWithAssertions = assertGenerator.generateAsserts(classTest, currentTestList);
            if (testWithAssertions.isEmpty()) {
                continue;
            } else {
                currentTestList = testWithAssertions;
            }
            JunitResult result = compileAndRunTests(classTest, currentTestList);
            if (result == null) {
                continue;
            } else if (!result.getFailures().isEmpty()) {
                Log.warn("Discarding failing test cases");
                final List<String> failingTestCase = result.getFailures().stream()
                        .collect(ArrayList<String>::new,
                                (testHeader, failure) -> testHeader.add(failure.getTestHeader()),
                                ArrayList<String>::addAll);
                currentTestList = currentTestList.stream()
                        .filter(ctMethod -> !failingTestCase.contains(ctMethod.getSimpleName()))
                        .collect(Collectors.toList());
            }
            currentTestList = AmplificationHelper.filterTest(currentTestList, result);
            Log.debug("{} test method(s) has been successfully generated", currentTestList.size());
            amplifiedTests.addAll(testSelector.selectToKeep(currentTestList));
        }
        return amplifiedTests;
    }

    private void updateAmplifiedTestList(List<CtMethod<?>> ampTest, List<CtMethod<?>> amplification) {
        ampTest.addAll(amplification);
        ampTestCount += amplification.size();
        Log.debug("total amp test: {}, global: {}", amplification.size(), ampTestCount);
    }

    private List<CtMethod<?>> preAmplification(CtType classTest, List<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        if (tests.isEmpty()) {
            return Collections.emptyList();
        }
        JunitResult result = compileAndRunTests(classTest, tests);
        if (result == null) {
            Log.warn("Need a green test suite to run dspot");
            return Collections.emptyList();
        }
        if (!result.getFailures().isEmpty()) {
            Log.warn("{} tests failed before the amplifications", result.getFailures().size());
            Log.warn("{}", result.getFailures().stream().reduce("",
                    (s, failure) -> s + failure.getTestHeader() + ":" + failure.getException() + System.getProperty("line.separator"),
                    String::concat)
            );
            Log.warn("Discarding following test cases for the amplification");

            result.getFailures().forEach(failure -> {
                try {
                    String methodName = failure.getTestHeader();
                    CtMethod testToRemove = tests.stream()
                            .filter(m -> methodName.startsWith(m.getSimpleName()))
                            .findFirst().get();
                    tests.remove(tests.indexOf(testToRemove));
                    Log.warn("{}", testToRemove.getSimpleName());
                } catch (Exception ignored) {
                    //ignored
                }
            });
            return preAmplification(classTest, tests);
        } else {
            testSelector.update();
            resetAmplifiers(classTest);
            Log.debug("Try to add assertions before amplification");
            List<CtMethod<?>> preAmplifiedMethods = testSelector.selectToKeep(
                    assertGenerator.generateAsserts(
                            classTest, testSelector.selectToAmplify(tests))
            );
            if (tests.containsAll(preAmplifiedMethods)) {
                return Collections.emptyList();
            } else {
                return preAmplifiedMethods;
            }
        }
    }

    private List<CtMethod<?>> amplifyTests(List<CtMethod<?>> tests) {
        List<CtMethod<?>> amplifiedTests = tests.stream()
                .flatMap(test -> amplifyTest(test).stream())
                .filter(test -> test != null && !test.getBody().getStatements().isEmpty())
                .collect(Collectors.toList());
        Log.debug("{} new tests generated", amplifiedTests.size());
        return amplifiedTests;
    }

    private List<CtMethod<?>> amplifyTest(CtMethod test) {
        return amplifiers.stream()
                .flatMap(amplifier -> amplifier.apply(test).stream())
                .map(amplifiedTest ->
                        AmplificationHelper.addOriginInComment(amplifiedTest, AmplificationHelper.getTopParent(test))
                ).collect(Collectors.toList());
    }

    private void resetAmplifiers(CtType parentClass) {
        amplifiers.forEach(amp -> amp.reset(parentClass));
    }

    private JunitResult compileAndRunTests(CtType classTest, List<CtMethod<?>> currentTestList) {
        CtType amplifiedTestClass = this.testSelector.buildClassForSelection(classTest, currentTestList);
        boolean status = TestCompiler.writeAndCompile(this.compiler, amplifiedTestClass, false,
                this.inputProgram.getProgramDir() + this.inputProgram.getClassesDir() + "/:" +
                        this.inputProgram.getProgramDir() + this.inputProgram.getTestClassesDir() + "/");
        if (!status) {
            Log.debug("Unable to compile {}", amplifiedTestClass.getSimpleName());
            return null;
        }
        JunitResult result;
        try {
            String classpath = AmplificationHelper.getClassPath(this.compiler, this.inputProgram);
            result = TestRunner.runTests(amplifiedTestClass, currentTestList, classpath, this.inputProgram);
        } catch (Exception ignored) {
            Log.debug("Error during running test");
            return null;
        }
        if (result == null || result.getTestRuns().size() < currentTestList.size()) {
            Log.debug("Error during running test");
            return null;
        }
        Log.debug("update test testCriterion");
        testSelector.update();
        return result;
    }
}