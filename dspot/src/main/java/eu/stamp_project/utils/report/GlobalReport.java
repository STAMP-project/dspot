package eu.stamp_project.utils.report;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public interface GlobalReport {

    public void addInputError(Error error);

    public void addError(Error error);

    public List<Error> getErrors();

    public List<Error> getInputError();

    void output();

    void reset();
}
