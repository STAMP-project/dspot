package eu.stamp_project.utils.report.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Main;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
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

    public Output(String absolutePathToProjectRoot, String outputDirectoryPath) {
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
            DSpotUtils.printAndCompileToCheck(amplification, outputDirectory);
        } else {
            LOGGER.warn("DSpot could not obtain any amplified test method.");
            LOGGER.warn("You can customize the following options: --amplifiers, --test-criterion, --iteration, --inputAmplDistributor etc, and retry with a new configuration.");
        }

        //TODO if something bad happened, the call to TestSelector#report() might throw an exception.
        //For now, I wrap it in a try/catch, but we might think of a better way to handle this.
        // TODO Externalize these statements
//        try {
//            Main.GLOBAL_REPORT.addTestSelectorReportForTestClass(testClassToBeAmplified, testSelector.report());
//        } catch (Exception e) {
//            e.printStackTrace();
//            LOGGER.error("Something bad happened during the report fot test-criterion.");
//            LOGGER.error("Dspot might not have output correctly!");
//        }

        this.writerProjectTimeJSON();
        return amplification;
    }

    private void writerProjectTimeJSON() {
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

    public static Output get(InputConfiguration configuration) {
        return new Output(configuration.getAbsolutePathToProjectRoot(), configuration.getOutputDirectory());
    }
}
