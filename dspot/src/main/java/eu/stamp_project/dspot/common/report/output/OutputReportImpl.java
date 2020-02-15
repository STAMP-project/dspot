package eu.stamp_project.dspot.common.report.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public class OutputReportImpl implements OutputReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutputReport.class);

    private List<String> linesOfOutputtingAmplifiedTestClasses;

    private int totalNumberOfAmplifiedTestMethods;

    public OutputReportImpl() {
        this.linesOfOutputtingAmplifiedTestClasses = new ArrayList<>();
        this.totalNumberOfAmplifiedTestMethods = 0;
    }

    @Override
    public void addNumberAmplifiedTestMethodsToTotal(int numberOfAmplifiedTestMethods) {
        this.totalNumberOfAmplifiedTestMethods += numberOfAmplifiedTestMethods;
    }

    @Override
    public void addPrintedTestClasses(String line) {
        this.linesOfOutputtingAmplifiedTestClasses.add(line);
    }

    @Override
    public void output(String outputDirectory) {
        LOGGER.info("The amplification ends up with {} amplified test methods over {} test classes.",
                this.totalNumberOfAmplifiedTestMethods,
                this.linesOfOutputtingAmplifiedTestClasses.size()
        );
        this.linesOfOutputtingAmplifiedTestClasses.forEach(LOGGER::info);
    }

    @Override
    public void reset() {
        this.linesOfOutputtingAmplifiedTestClasses.clear();
        this.totalNumberOfAmplifiedTestMethods = 0;
    }
}
