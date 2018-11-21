package eu.stamp_project.dspot.report;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public interface GlobalReport {

    public void addError(ErrorEnum error, Throwable e);

    public List<Error> getErrors();

    void output();
}
