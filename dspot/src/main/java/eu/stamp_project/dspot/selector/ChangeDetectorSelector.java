package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.selector.json.change.TestCaseJSON;
import eu.stamp_project.dspot.selector.json.change.TestClassJSON;
import eu.stamp_project.minimization.ChangeMinimizer;
import eu.stamp_project.minimization.Minimizer;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestRunner;
import org.codehaus.plexus.util.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/17
 */
public class ChangeDetectorSelector implements TestSelector {

    private String pathToFirstVersionOfProgram;

    private String pathToSecondVersionOfProgram;

    private Map<CtMethod<?>, Failure> failurePerAmplifiedTest;

    private CtType<?> currentClassTestToBeAmplified;

    public ChangeDetectorSelector() {
        this.failurePerAmplifiedTest = new HashMap<>();
    }

    @Override
    public void init(InputConfiguration configuration) {
        try {
            this.pathToFirstVersionOfProgram = InputConfiguration.get().getAbsolutePathToProjectRoot();
            this.pathToSecondVersionOfProgram = InputConfiguration.get().getAbsolutePathToSecondVersionProjectRoot();
            InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
            InputConfiguration.get().getBuilder().compile();
            InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToFirstVersionOfProgram);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null) {
            this.currentClassTestToBeAmplified = classTest;
            this.failurePerAmplifiedTest.clear();
        }
        return testsToBeAmplified;
    }


    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }
        CtType clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        this.currentClassTestToBeAmplified.getMethods().stream()
                .filter(AmplificationChecker::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC));

        InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
        DSpotCompiler.compile(
                InputConfiguration.get(),
                DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC,
                InputConfiguration.get().getFullClassPathWithExtraDependencies(),
                new File(this.pathToSecondVersionOfProgram + InputConfiguration.get().getPathToTestClasses())
        );

        final TestListener results;
        try {
            results = TestRunner.run(
                    InputConfiguration.get().getFullClassPathWithExtraDependencies(),
                    this.pathToSecondVersionOfProgram,
                    clone.getQualifiedName(),
                    amplifiedTestToBeKept.stream()
                            .map(CtMethod::getSimpleName)
                            .toArray(String[]::new));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToFirstVersionOfProgram);
        }
        final List<CtMethod<?>> amplifiedThatWillBeKept = new ArrayList<>();
        if (!results.getFailingTests().isEmpty()) {
            results.getFailingTests()
                    .forEach(failure -> {
                                final CtMethod<?> key = amplifiedTestToBeKept.stream()
                                        .filter(ctMethod -> ctMethod.getSimpleName().equals(failure.testCaseName))
                                        .findFirst()
                                        .get();
                                amplifiedThatWillBeKept.add(key);
                                this.failurePerAmplifiedTest.put(key, failure);
                            }
                    );
        }
        return amplifiedThatWillBeKept;
    }

    @Override
    public List<CtMethod<?>> getAmplifiedTestCases() {
        return new ArrayList<>(this.failurePerAmplifiedTest.keySet());
    }

    @Override
    public Minimizer getMinimizer() {
        return new ChangeMinimizer(
                this.currentClassTestToBeAmplified,
                InputConfiguration.get(),
                this.failurePerAmplifiedTest
        );
    }

    protected void reset() {
        this.currentClassTestToBeAmplified = null;
    }

    @Override
    public void report() {
        final String output = "======= REPORT =======" + AmplificationHelper.LINE_SEPARATOR +
                this.failurePerAmplifiedTest.size() + " amplified test fails on the new versions." +
                AmplificationHelper.LINE_SEPARATOR +
                this.failurePerAmplifiedTest.keySet()
                        .stream()
                        .reduce("",
                                (acc, amplifiedTest) -> acc +
                                        this.failurePerAmplifiedTest.get(amplifiedTest).toString() +
                                        AmplificationHelper.LINE_SEPARATOR,
                                String::concat);
        System.out.println(output);
        try {
            FileUtils.forceMkdir(new File(InputConfiguration.get().getOutputDirectory() + "/" +
                    this.currentClassTestToBeAmplified.getQualifiedName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(new File(InputConfiguration.get().getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_change_report.txt"))) {
            writer.write(output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(new File(InputConfiguration.get().getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_stacktraces.txt"))) {
            final PrintWriter printWriter = new PrintWriter(writer);
            this.failurePerAmplifiedTest.keySet()
                    .forEach(amplifiedTest ->
                            printWriter.write(this.failurePerAmplifiedTest.get(amplifiedTest).stackTrace)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.reportJson();
        this.reset();
    }

    private void reportJson() {
        if (this.currentClassTestToBeAmplified == null) {
            return;
        }
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(InputConfiguration.get().getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_change_detector.json");
        if (file.exists()) {
            try {
                testClassJSON = gson.fromJson(new FileReader(file), TestClassJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            testClassJSON = new TestClassJSON(
                    this.currentClassTestToBeAmplified.getQualifiedName(),
                    AmplificationHelper.getAllTest(this.currentClassTestToBeAmplified).size()
            );
        }
        this.getAmplifiedTestCases().stream()
                .map(ctMethod ->
                    new TestCaseJSON(ctMethod.getSimpleName(), Counter.getInputOfSinceOrigin(ctMethod), Counter.getAssertionOfSinceOrigin(ctMethod))
                ).forEach(testClassJSON.testCases::add);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(testClassJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
