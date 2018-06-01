package eu.stamp_project.dspot.selector;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.Initializer;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import eu.stamp_project.minimization.ChangeMinimizer;
import eu.stamp_project.minimization.Minimizer;
import org.codehaus.plexus.util.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/17
 */
public class ChangeDetectorSelector implements TestSelector {

    private String pathToChangedVersionOfProgram;

    private Map<CtMethod<?>, Failure> failurePerAmplifiedTest;

    private InputConfiguration configuration;

    private InputConfiguration changedConfiguration;


    private CtType<?> currentClassTestToBeAmplified;

    public ChangeDetectorSelector() {
        this.failurePerAmplifiedTest = new HashMap<>();
    }

    @Override
    public void init(InputConfiguration configuration) {
        this.configuration = configuration;
        final String configurationPath = configuration.getProperty("configPath");
        final String pathToFolder = configuration.getProperty("folderPath");
        try {
            changedConfiguration = new InputConfiguration(configurationPath);
            this.pathToChangedVersionOfProgram = DSpotUtils.shouldAddSeparator.apply(pathToFolder);
            if (this.configuration.getProperty("targetModule") != null) {
                this.pathToChangedVersionOfProgram +=
                        DSpotUtils.shouldAddSeparator.apply(this.configuration.getProperty("targetModule"));
                configuration.getProperties().setProperty("targetModule", this.configuration.getProperty("targetModule"));
            }
            changedConfiguration.setAbsolutePathToProjectRoot(new File(this.pathToChangedVersionOfProgram).getAbsolutePath());
            Initializer.initialize(changedConfiguration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
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

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.pathToTmpTestSources));
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .buildClasspath()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/";

        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources,
                classpath + AmplificationHelper.PATH_SEPARATOR + this.changedConfiguration.getClasspathClassesProject(),
                new File(this.pathToChangedVersionOfProgram + this.changedConfiguration.getPathToTestClasses()));

        final TestListener results;
        try {
            results = EntryPoint.runTests(classpath + AmplificationHelper.PATH_SEPARATOR +
                            this.changedConfiguration.getClasspathClassesProject(),
                    clone.getQualifiedName(),
                    amplifiedTestToBeKept.stream()
                            .map(CtMethod::getSimpleName)
                            .toArray(String[]::new)

            );
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        if (!results.getFailingTests().isEmpty()) {
            results.getFailingTests()
                    .forEach(failure ->
                            this.failurePerAmplifiedTest.put(
                                    amplifiedTestToBeKept.stream()
                                            .filter(ctMethod ->
                                                    ctMethod.getSimpleName().equals(failure.testCaseName)
                                            ).findFirst()
                                            .get(), failure)
                    );
        }
        return amplifiedTestToBeKept;
    }

    @Override
    public List<CtMethod<?>> getAmplifiedTestCases() {
        return new ArrayList<>(this.failurePerAmplifiedTest.keySet());
    }

    @Override
    public Minimizer getMinimizer() {
        return new ChangeMinimizer(
                this.currentClassTestToBeAmplified,
                this.configuration,
                this.changedConfiguration,
                this.pathToChangedVersionOfProgram,
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
            FileUtils.forceMkdir(new File(this.configuration.getOutputDirectory() + "/" +
                    this.currentClassTestToBeAmplified.getQualifiedName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(new File(this.configuration.getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_change_report.txt"))) {
            writer.write(output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(new File(this.configuration.getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_stacktraces.txt"))) {
            final PrintWriter printWriter = new PrintWriter(writer);
            this.failurePerAmplifiedTest.keySet()
                    .forEach(amplifiedTest ->
                            printWriter.write(this.failurePerAmplifiedTest.get(amplifiedTest).stackTrace)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.reset();
    }
}
