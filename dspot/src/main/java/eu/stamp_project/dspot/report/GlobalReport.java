package eu.stamp_project.dspot.report;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public interface GlobalReport {

    public void addError(ErrorEnum error, Throwable e);

}
