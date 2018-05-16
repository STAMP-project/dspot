package eu.stamp_project.dspot;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/05/18
 */
public class AmplificationException extends Exception {

    public AmplificationException(String message) {
        super(message);
    }

    public AmplificationException(Throwable cause) {
        super(cause);
    }

    public AmplificationException(String message, Throwable cause) {
        super(message, cause);
    }

}
