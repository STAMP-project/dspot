package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.utils.report.output.selector.change.json.TestCaseJSON;
import eu.stamp_project.utils.report.output.selector.change.json.TestClassJSON;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.execution.TestRunner;
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
    public boolean init() {
        try {
            this.pathToFirstVersionOfProgram = InputConfiguration.get().getAbsolutePathToProjectRoot();
            this.pathToSecondVersionOfProgram = InputConfiguration.get().getAbsolutePathToSecondVersionProjectRoot();
            InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
            DSpotPOMCreator.createNewPom();
            InputConfiguration.get().getBuilder().compile();
            InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToFirstVersionOfProgram);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
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
                .filter(TestFramework.get()::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
        final String pathToAmplifiedTestSrc = DSpotCompiler.getPathToAmplifiedTestSrc();

        InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
        DSpotCompiler.compile(
                InputConfiguration.get(),
                pathToAmplifiedTestSrc,
                InputConfiguration.get().getFullClassPathWithExtraDependencies(),
                new File(this.pathToSecondVersionOfProgram + InputConfiguration.get().getPathToTestClasses())
        );

        final TestResult results;
        try {
            InputConfiguration.get().setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
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

    protected void reset() {
        this.currentClassTestToBeAmplified = null;
    }

    @Override
    public TestSelectorElementReport report() {
        final StringBuilder output = new StringBuilder();
        output.append(this.failurePerAmplifiedTest.size()).append(" amplified test fails on the new versions.");
        this.failurePerAmplifiedTest.keySet()
                .stream()
                .map(this.failurePerAmplifiedTest::get)
                .map(Object::toString)
                .map(AmplificationHelper.LINE_SEPARATOR::concat)
                .forEachOrdered(output::append);
        this.failurePerAmplifiedTest.keySet()
                .forEach(amplifiedTest ->
                        output.append(this.failurePerAmplifiedTest.get(amplifiedTest).stackTrace)
                );
        final TestClassJSON testClassJSON = this.reportJson();
        this.reset();
        return new TestSelectorElementReportImpl(output.toString(), testClassJSON);
    }

    private TestClassJSON reportJson() {
        if (this.currentClassTestToBeAmplified == null) {
            return null;
        }
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(InputConfiguration.get().getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "report.json");
        if (file.exists()) {
            try {
                testClassJSON = gson.fromJson(new FileReader(file), TestClassJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            testClassJSON = new TestClassJSON(
                    this.currentClassTestToBeAmplified.getQualifiedName(),
                    TestFramework.getAllTest(this.currentClassTestToBeAmplified).size()
            );
        }
        this.failurePerAmplifiedTest.keySet().stream()
                .map(ctMethod ->
                        new TestCaseJSON(ctMethod.getSimpleName(), Counter.getInputOfSinceOrigin(ctMethod), Counter.getAssertionOfSinceOrigin(ctMethod))
                ).forEach(testClassJSON.testCases::add);
        return testClassJSON;
    }
}
