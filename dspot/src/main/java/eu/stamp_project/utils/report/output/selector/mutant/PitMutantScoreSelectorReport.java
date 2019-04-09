package eu.stamp_project.utils.report.output.selector.mutant;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.GlobalReport;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.utils.report.output.selector.TestSelectorReport;
import org.osgi.service.log.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public class PitMutantScoreSelectorReport implements TestSelectorReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitMutantScoreSelectorReport.class);

    private Map<CtType<?>, PitMutantScoreSelectorElementReport> testSelectorElementReportPerTestClass;

    public PitMutantScoreSelectorReport() {
        this.testSelectorElementReportPerTestClass = new HashMap<>();
    }

    @Override
    public void output() {
        final String allReports = this.testSelectorElementReportPerTestClass.keySet().stream()
                .map(testClass -> this.testSelectorElementReportPerTestClass.get(testClass).output(testClass))
                .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
        LOGGER.info("{}{}", AmplificationHelper.LINE_SEPARATOR, allReports);
        try (FileWriter writer = new FileWriter(
                DSpotUtils.shouldAddSeparator.apply(InputConfiguration.get().getOutputDirectory()) +
                        "mutants_report.txt", false)
        ) {
            writer.write(allReports);
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
        this.testSelectorElementReportPerTestClass.put(testClass, (PitMutantScoreSelectorElementReport) report);
    }
}
