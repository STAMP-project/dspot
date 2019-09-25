package eu.stamp_project.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Main;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.json.ClassTimeJSON;
import eu.stamp_project.utils.json.ProjectTimeJSON;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSpot.class);

    private TestSelector testSelector;

    private ProjectTimeJSON projectTimeJSON;

    public DSpot(TestSelector testSelector) {
        this.testSelector = testSelector;
        String splitter = File.separator.equals("/") ? "/" : "\\\\";
        final String[] splittedPath = InputConfiguration.get().getAbsolutePathToProjectRoot().split(splitter);
        final File projectJsonFile = new File(InputConfiguration.get().getOutputDirectory() +
                File.separator + splittedPath[splittedPath.length - 1] + ".json");
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

    public CtType<?> amplify(Amplification testAmplification, CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified) {
        Counter.reset();
        if (InputConfiguration.get().shouldGenerateAmplifiedTestClass()) {
            testClassToBeAmplified = AmplificationHelper.renameTestClassUnderAmplification(testClassToBeAmplified);
        }
        long time = System.currentTimeMillis();
        testAmplification.amplification(testClassToBeAmplified, testMethodsToBeAmplified);
        final long elapsedTime = System.currentTimeMillis() - time;
        LOGGER.info("elapsedTime {}", elapsedTime);
        this.projectTimeJSON.add(new ClassTimeJSON(testClassToBeAmplified.getQualifiedName(), elapsedTime));
        final CtType clone = testClassToBeAmplified.clone();
        testClassToBeAmplified.getPackage().addType(clone);
        final CtType<?> amplification = AmplificationHelper.createAmplifiedTest(testSelector.getAmplifiedTestCases(), clone);
        final File outputDirectory = new File(InputConfiguration.get().getOutputDirectory());
        //Optimization: this object is not required anymore
        //and holds a dictionary with large number of cloned CtMethods.
        testAmplification = null;
        //Optimization: this.testSelector.getAmplifiedTestCases() also holds a large number of cloned CtMethods,
        //but it is clear before iterating again for next test class
        LOGGER.debug("OPTIMIZATION: GC invoked");
        System.gc(); //Optimization: cleaning up heap before printing the amplified class
        if (!testSelector.getAmplifiedTestCases().isEmpty()) {
            Main.GLOBAL_REPORT.addNumberAmplifiedTestMethodsToTotal(testSelector.getAmplifiedTestCases().size());
            Main.GLOBAL_REPORT.addPrintedTestClasses(
                    String.format("Print %s with %d amplified test cases in %s",
                    amplification.getQualifiedName() + ".java",
                    testSelector.getAmplifiedTestCases().size(),
                    InputConfiguration.get().getOutputDirectory())
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
        try {
            Main.GLOBAL_REPORT.addTestSelectorReportForTestClass(testClassToBeAmplified, testSelector.report());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Something bad happened during the report fot test-criterion.");
            LOGGER.error("Dspot might not have output correctly!");
        }
        writeTimeJson();
        InputConfiguration.get().getBuilder().reset();
        return amplification;
    }

    private void writeTimeJson() {
        final File file1 = new File(InputConfiguration.get().getOutputDirectory());
        if (!file1.exists()) {
            file1.mkdir();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(InputConfiguration.get().getOutputDirectory() +
                "/" + this.projectTimeJSON.projectName + ".json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(this.projectTimeJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
