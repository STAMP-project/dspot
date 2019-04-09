package eu.stamp_project.utils.report.output.selector.mutant;

import com.google.gson.GsonBuilder;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.utils.report.output.selector.mutant.json.TestClassJSON;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public class PitMutantScoreSelectorElementReport implements TestSelectorElementReport {

    private final static String SUFFIX_PATH_TO_JSON_FILE = "_mutants_killed.json";

    private String textualReport;

    private TestClassJSON testClassJSON;

    public PitMutantScoreSelectorElementReport(String textualReport, TestClassJSON testClassJSON) {
        this.textualReport = textualReport;
        this.testClassJSON = testClassJSON;
    }

    public String output(CtType<?> testClass) {
        // 1 output the specifc JSON file for the test class
        final File outputJsonFile = new File(
                DSpotUtils.shouldAddSeparator.apply(InputConfiguration.get().getOutputDirectory()) +
                        testClass.getQualifiedName() + SUFFIX_PATH_TO_JSON_FILE
        );
        try (FileWriter writer = new FileWriter(outputJsonFile, false)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(testClassJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 2 return the textual report for this test class
        return this.textualReport;
    }

}
