package eu.stamp_project.dspot.report;

import eu.stamp_project.dspot.Amplification;
import eu.stamp_project.program.InputConfiguration;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Amplification.class);

    private List<Error> errors;

    public GlobalReportImpl() {
        this.errors = new ArrayList<>();
    }

    @Override
    public List<Error> getErrors() {
        return errors;
    }

    @Override
    public void output() {
        if (this.errors.isEmpty()) {
            LOGGER.info("DSpot amplified your test suite without errors. (no errors report will be outputted)");
        } else {
            LOGGER.warn("DSpot encountered some errors during the amplification.");
            final StringBuilder report = new StringBuilder();
            report.append(String.format("DSpot encountered %d error(s) during amplification.", this.errors.size()))
                    .append(AmplificationHelper.LINE_SEPARATOR)
                    .append(
                            this.errors.stream().map(Error::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
                    );
            final String stringReport = report.toString();
            LOGGER.warn(stringReport);
            try (FileWriter writer = new FileWriter(InputConfiguration.get().getOutputDirectory() + "/errors_report.txt", false)) {
                writer.write(stringReport);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void addError(ErrorEnum error, Throwable throwable) {
        LOGGER.warn(error.getMessage());
        this.errors.add(new Error(error, throwable));
    }
}


