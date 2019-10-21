package eu.stamp_project.utils.report.output.selector;

import com.google.gson.GsonBuilder;
import eu.stamp_project.utils.DSpotUtils;

import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public class TestSelectorElementReportImpl implements TestSelectorElementReport {

    private final static String SUFFIX_PATH_TO_JSON_FILE = "_report.json";

    private String textualReport;

    private TestClassJSON testClassJSON;

    private List<String> testCriterionReports;

    private String extension;

    public TestSelectorElementReportImpl(String textualReport,
                                         TestClassJSON testClassJSON,
                                         List<String> testCriterionReports,
                                         String extension) {
        this.textualReport = textualReport;
        this.testClassJSON = testClassJSON;
        this.testCriterionReports = testCriterionReports;
        this.extension = extension;
    }

    @Override
    public String getReportForCollector() {
        return this.testClassJSON.toString();
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
        // 2 output the baseline, intermediate and final test criterion reports
        final String reportPathName = DSpotUtils.shouldAddSeparator
                .apply(outputDirectory) + testClass.getQualifiedName().replaceAll("\\.", "_")
                + "_test_criterion_report_";
        testCriterionReports.forEach(testCriterionReportContent -> {
                    try (FileWriter writer =
                                 new FileWriter(reportPathName + testCriterionReports.indexOf(testCriterionReportContent) + this.extension, false)) {
                        writer.write(testCriterionReportContent);
                    } catch (Exception e) {
                        //ignored
                    }
                }
        );
        // 3 return the textual report for this test classTestClassJSON
        return this.textualReport;
    }

}
