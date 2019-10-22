package eu.stamp_project.utils.report.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Main;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.collector.Collector;
import eu.stamp_project.utils.collector.NullCollector;
import eu.stamp_project.utils.json.ClassTimeJSON;
import eu.stamp_project.utils.json.ProjectTimeJSON;
import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/09/19
 */
public class Output {

    private static final Logger LOGGER = LoggerFactory.getLogger(Output.class);

    private ProjectTimeJSON projectTimeJSON;

    private String outputPathDirectory;

    private Collector collector;

    public Output(String absolutePathToProjectRoot, String outputDirectoryPath, Collector collector) {
        this.outputPathDirectory = outputDirectoryPath;
        String splitter = File.separator.equals("/") ? "/" : "\\\\";
        final String[] splittedPath = absolutePathToProjectRoot.split(splitter);
        final File projectJsonFile = new File(outputDirectoryPath + File.separator + splittedPath[splittedPath.length - 1] + ".json");
        if (projectJsonFile.exists()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try {
                this.projectTimeJSON = gson.fromJson(new FileReader(projectJsonFile), ProjectTimeJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.projectTimeJSON = new ProjectTimeJSON(splittedPath[splittedPath.length - 1]);
        }
        this.collector = collector;
    }

    public CtType<?> output(CtType<?> testClassToBeAmplified, List<CtMethod<?>> amplifiedTestMethods) {
        final CtType clone = testClassToBeAmplified.clone();
        testClassToBeAmplified.getPackage().addType(clone);
        final CtType<?> amplification = AmplificationHelper.createAmplifiedTest(amplifiedTestMethods, clone);
        final File outputDirectory = new File(this.outputPathDirectory);
        if (!amplifiedTestMethods.isEmpty()) {
            Main.GLOBAL_REPORT.addNumberAmplifiedTestMethodsToTotal(amplifiedTestMethods.size());
            Main.GLOBAL_REPORT.addPrintedTestClasses(
                    String.format("Print %s with %d amplified test cases in %s",
                            amplification.getQualifiedName() + ".java",
                            amplifiedTestMethods.size(),
                            this.outputPathDirectory
                    )
            );
            // we try to compile the newly generated amplified test class (.java)
            // if this fail, we re-print the java test class without imports
            DSpotUtils.printAndCompileToCheck(amplification, outputDirectory, collector);
        } else {
            LOGGER.warn("DSpot could not obtain any amplified test method.");
            LOGGER.warn("You can customize the following options: --amplifiers, --test-criterion, --iteration, --inputAmplDistributor etc, and retry with a new configuration.");
        }
        this.writeProjectTimeJSON();
        // output the original test class with original test methods
        outputSeedTestClassWithSuccessTestMethods(testClassToBeAmplified, amplifiedTestMethods);
        return amplification;
    }

    private void outputSeedTestClassWithSuccessTestMethods(CtType<?> testClassToBeAmplified,
                                                           List<CtMethod<?>> amplifiedTestMethods) {
        final CtType<?> clone = testClassToBeAmplified.clone();
        testClassToBeAmplified.getPackage().addType(clone);
        clone.getMethods().stream()
                .filter(TestFramework.get()::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestMethods.stream()
                .map(AmplificationHelper::getOriginalTestMethod)
                .distinct()
                .forEach(clone::addMethod);
        final File outputDirectory = new File(this.outputPathDirectory + "/original/");
        Main.GLOBAL_REPORT.addNumberAmplifiedTestMethodsToTotal(amplifiedTestMethods.size());
        Main.GLOBAL_REPORT.addPrintedTestClasses(
                String.format("Print %s with %d amplified test cases in %s",
                        clone.getQualifiedName() + ".java",
                        amplifiedTestMethods.size(),
                        this.outputPathDirectory + "/original/"
                )
        );
        DSpotUtils.printCtTypeToGivenDirectory(clone, outputDirectory, true);
    }

    private void writeProjectTimeJSON() {
        final File file1 = new File(this.outputPathDirectory);
        if (!file1.exists()) {
            file1.mkdir();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.outputPathDirectory + "/" + this.projectTimeJSON.projectName + ".json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(this.projectTimeJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addClassTimeJSON(String qualifiedName, long elapsedTime) {
        this.projectTimeJSON.add(new ClassTimeJSON(qualifiedName, elapsedTime));
    }

    public void reportSelectorInformation(String report) {
        this.collector.reportSelectorInformation(report);
    }

    public static Output get(InputConfiguration configuration, Collector collector) {
        return new Output(configuration.getAbsolutePathToProjectRoot(), configuration.getOutputDirectory(), collector);
    }

    public static Output get(InputConfiguration configuration) {
        return new Output(configuration.getAbsolutePathToProjectRoot(), configuration.getOutputDirectory(), new NullCollector());
    }
}
