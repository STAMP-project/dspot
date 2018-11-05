package eu.stamp_project.dspot.report;

import eu.stamp_project.dspot.Amplification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
    public void addError(ErrorEnum error, Throwable throwable) {
        LOGGER.warn(error.getMessage());
        this.errors.add(new Error(error, throwable));
    }
}

