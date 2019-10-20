package eu.stamp_project.utils.report.output.selector;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public class TestSelectorReportImpl implements TestSelectorReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSelectorReport.class);

    private Map<CtType<?>, TestSelectorElementReport> testSelectorElementReportPerTestClass;

    public TestSelectorReportImpl() {
        this.testSelectorElementReportPerTestClass = new HashMap<>();
    }

    @Override
    public void output(String outputDirectory) {
        final String allReports = this.testSelectorElementReportPerTestClass.keySet()
                .stream()
                .filter(testClass -> this.testSelectorElementReportPerTestClass.get(testClass) != null)
                .map(testClass -> this.testSelectorElementReportPerTestClass.get(testClass).output(
                        testClass, outputDirectory)
                ).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
        LOGGER.info("{}{}", AmplificationHelper.LINE_SEPARATOR, allReports);
        final String reportPathName = DSpotUtils.shouldAddSeparator
                .apply(outputDirectory) +
                "report.txt";
        try (FileWriter writer = new FileWriter(reportPathName, false)) {
            writer.write(allReports);
            LOGGER.info("Writing report in {}", reportPathName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() {
        this.testSelectorElementReportPerTestClass.clear();
    }

    @Override
    public void addTestSelectorReportForTestClass(CtType<?> testClass, TestSelectorElementReport report) {
        this.testSelectorElementReportPerTestClass.put(testClass, (TestSelectorElementReportImpl) report);
    }
}
