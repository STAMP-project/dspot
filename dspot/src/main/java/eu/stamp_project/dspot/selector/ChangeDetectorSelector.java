package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.dspot.common.report.output.selector.change.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.change.json.TestClassJSON;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/17
 */
public class ChangeDetectorSelector extends AbstractTestSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeDetectorSelector.class);

    private String pathToFirstVersionOfProgram;

    private String pathToSecondVersionOfProgram;

    private Map<CtMethod<?>, Failure> failurePerAmplifiedTest;

    private CtType<?> currentClassTestToBeAmplified;

    private String secondVersionTargetClasses;

    public ChangeDetectorSelector(AutomaticBuilder automaticBuilder,
                                  UserInput configuration) {
        super(automaticBuilder, configuration);
        this.failurePerAmplifiedTest = new HashMap<>();
        this.pathToFirstVersionOfProgram = DSpotUtils.shouldAddSeparator.apply(configuration.getAbsolutePathToProjectRoot());
        this.pathToSecondVersionOfProgram = DSpotUtils.shouldAddSeparator.apply(configuration.getAbsolutePathToSecondVersionProjectRoot());
        try {
            this.automaticBuilder.setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
            configuration.setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
            this.secondVersionTargetClasses = configuration.getClasspathClassesProject();
            DSpotPOMCreator.createNewPom(configuration);
            this.automaticBuilder.compile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.automaticBuilder.setAbsolutePathToProjectRoot(this.pathToFirstVersionOfProgram);
            configuration.setAbsolutePathToProjectRoot(this.pathToFirstVersionOfProgram);
        }
    }

    @Override
    public boolean init() {

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

        this.automaticBuilder.setAbsolutePathToProjectRoot(this.pathToSecondVersionOfProgram);
        if (!DSpotCompiler.compile(
                pathToAmplifiedTestSrc,
                this.classpath + AmplificationHelper.PATH_SEPARATOR + this.secondVersionTargetClasses,
                new File(this.pathToSecondVersionOfProgram + this.pathToTestClasses)
        )) {
            LOGGER.warn("Something went bad during the compilation of the amplified test methods using the second version.");
            // add an error in the Main Global error report
        }
        final TestResult results;
        try {
            results = this.testRunner.run(
                    this.classpath + AmplificationHelper.PATH_SEPARATOR + this.secondVersionTargetClasses,
                    this.pathToSecondVersionOfProgram,
                    clone.getQualifiedName(),
                    amplifiedTestToBeKept.stream()
                            .map(CtMethod::getSimpleName)
                            .toArray(String[]::new));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.automaticBuilder.setAbsolutePathToProjectRoot(this.pathToFirstVersionOfProgram);
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
        return new TestSelectorElementReportImpl(output.toString(), testClassJSON, Collections.emptyList(), "");
    }

    private TestClassJSON reportJson() {
        if (this.currentClassTestToBeAmplified == null) {
            return null;
        }
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.outputDirectory + "/" + this.currentClassTestToBeAmplified.getQualifiedName() + "report.json");
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
