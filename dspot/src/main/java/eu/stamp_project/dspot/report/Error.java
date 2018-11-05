package eu.stamp_project.dspot.report;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/11/18
 */
public class Error {

    public final ErrorEnum type;

    public final Throwable error;

    public Error(ErrorEnum type, Throwable error) {
        this.type = type;
        this.error = error;
    }
}
