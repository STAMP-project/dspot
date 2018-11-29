package eu.stamp_project.utils.report;

import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public class GlobalReportImpl implements GlobalReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalReport.class);

    protected List<Error> errors;

    protected List<Error> inputErrors;

    public GlobalReportImpl() {
        this.errors = new ArrayList<>();
        this.inputErrors = new ArrayList<>();
    }

    @Override
    public void reset() {
        this.errors.clear();
        this.inputErrors.clear();
    }

    @Override
    public List<Error> getErrors() {
        return errors;
    }

    @Override
    public List<Error> getInputError() {
        return null;
    }

    private boolean hasError() {
        return !(this.errors.isEmpty() && this.inputErrors.isEmpty());
    }

    @Override
    public void output() {
        if (!hasError()) {
            LOGGER.info("DSpot amplified your test suite without errors. (no errors report will be outputted)");
        } else {
            final StringBuilder report = new StringBuilder();
            if (!this.inputErrors.isEmpty()) {
                LOGGER.error("DSpot encountered some input errors.");
                displayAndAppendErrors(this.inputErrors, report, "DSpot encountered %d input error(s).");
            }
            if (!this.errors.isEmpty()) {
                LOGGER.warn("DSpot encountered some errors during amplification.");
                displayAndAppendErrors(this.errors, report, "DSpot encountered %d error(s) during amplification.");
            }
            final String stringReport = report.toString();
            LOGGER.warn(stringReport);
            try (FileWriter writer = new FileWriter(InputConfiguration.get().getOutputDirectory() + "/errors_report.txt", false)) {
                writer.write(stringReport);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void displayAndAppendErrors(List<Error> currentErrors, StringBuilder report, String intro) {
        report.append(String.format(intro, currentErrors.size()))
                .append(AmplificationHelper.LINE_SEPARATOR)
                .append(
                        currentErrors.stream().map(Error::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
                );
    }

    @Override
    public void addInputError(Error error) {
        this.addErrorToGivenList(this.inputErrors, error);
    }

    @Override
    public void addError(Error error) {
        this.addErrorToGivenList(this.errors, error);
    }

    protected void addErrorToGivenList(List<Error> givenErrors, Error newError) {
        LOGGER.warn(newError.toString());
        givenErrors.add(newError);
    }
}