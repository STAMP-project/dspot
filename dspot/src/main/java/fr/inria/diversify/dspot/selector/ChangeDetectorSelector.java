package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.test.launcher.TestLauncher;
import fr.inria.stamp.test.listener.TestListener;
import org.codehaus.plexus.util.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/17
 */
public class ChangeDetectorSelector extends TakeAllSelector {

    private String pathToChangedVersionOfProgram;

    @Override
    public void init(InputConfiguration configuration) {
        super.init(configuration);
        final String configurationPath = configuration.getProperty("configPath");
        final String pathToFolder = configuration.getProperty("folderPath");
        InputConfiguration inputConfiguration;
        try {
            inputConfiguration = new InputConfiguration(configurationPath);
            InputProgram inputProgram = InputConfiguration.initInputProgram(inputConfiguration);
            inputConfiguration.setInputProgram(inputProgram);
            this.pathToChangedVersionOfProgram = pathToFolder +
                    DSpotUtils.shouldAddSeparator.apply(pathToFolder) +
                    (inputConfiguration.getProperty("targetModule") != null ?
                            inputConfiguration.getProperty("targetModule") +
                                    DSpotUtils.shouldAddSeparator.apply(pathToFolder)
                            : "");
            inputProgram.setProgramDir(this.pathToChangedVersionOfProgram);
            String dependencies = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration)
                    .buildClasspath(inputProgram.getProgramDir());
            File output = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
            try {
                FileUtils.cleanDirectory(output);
            } catch (IllegalArgumentException ignored) {
                //the target directory does not exist, do not need to clean it
            }

            DSpotCompiler.compile(this.pathToChangedVersionOfProgram +
                            inputProgram.getRelativeSourceCodeDir() +
                            DSpotUtils.shouldAddSeparator.apply(inputProgram.getRelativeSourceCodeDir()),
                    dependencies, output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {

        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration)
                .buildClasspath(this.program.getProgramDir()) +
                System.getProperty("path.separator") +
                this.pathToChangedVersionOfProgram + "/" + this.program.getClassesDir() +
                System.getProperty("path.separator") +
                this.program.getProgramDir() + "/" + this.program.getTestClassesDir();

        final TestListener results = TestLauncher.run(this.configuration,
                classpath,
                this.currentClassTestToBeAmplified,
                amplifiedTestToBeKept.stream()
                        .map(CtNamedElement::getSimpleName)
                        .collect(Collectors.toList())
        );

        if (!results.getFailingTests().isEmpty()) {
            final List<String> failingTestName = results.getFailingTests()
                    .stream()
                    .map(Failure::getDescription)
                    .map(Description::getMethodName)
                    .collect(Collectors.toList());
            amplifiedTestToBeKept
                    .stream()
                    .filter(ctMethod -> failingTestName.contains(ctMethod.getSimpleName()))
                    .forEach(this.selectedAmplifiedTest::add);
        }
        return amplifiedTestToBeKept;
    }

    @Override
    public void report() {
        final String output = "======= REPORT =======" + AmplificationHelper.LINE_SEPARATOR +
                this.selectedAmplifiedTest.size() + " amplified test fails on the new versions." +
                    AmplificationHelper.LINE_SEPARATOR;
        System.out.println(output);
        try (FileWriter writer = new FileWriter(new File(this.configuration.getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_change_report.txt"))) {
            writer.write(output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
