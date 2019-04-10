package eu.stamp_project.utils.report.error;

import eu.stamp_project.utils.report.Report;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/04/19
 */
public interface ErrorReport extends Report {

    public void addInputError(Error error);

    public void addError(Error error);

    public List<Error> getErrors();

    public List<Error> getInputError();

}
