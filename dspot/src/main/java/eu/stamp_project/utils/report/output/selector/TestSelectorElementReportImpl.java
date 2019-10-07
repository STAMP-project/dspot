package eu.stamp_project.utils.report.output.selector;

import com.google.gson.GsonBuilder;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public class TestSelectorElementReportImpl implements TestSelectorElementReport {

    private final static String SUFFIX_PATH_TO_JSON_FILE = "_report.json";

    private String textualReport;

    private TestClassJSON testClassJSON;

    public TestSelectorElementReportImpl(String textualReport, TestClassJSON testClassJSON) {
        this.textualReport = textualReport;
        this.testClassJSON = testClassJSON;
    }

    @Override
    public String output(CtType<?> testClass, String outputDirectory) {
        // 1 output the specific JSON file for the test class
        final File outputJsonFile = new File(
                DSpotUtils.shouldAddSeparator.apply(outputDirectory) +
                        testClass.getQualifiedName() + SUFFIX_PATH_TO_JSON_FILE
        );
        try (FileWriter writer = new FileWriter(outputJsonFile, false)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(testClassJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 2 return the textual report for this test classTestClassJSON
        return this.textualReport;
    }

}
