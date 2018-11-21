package eu.stamp_project.utils.report;

import eu.stamp_project.utils.AmplificationHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Function;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/11/18
 */
public class Error {

    public final ErrorEnum type;

    public final Throwable error;

    public final String message;

    public Error(ErrorEnum type, Throwable error) {
        this.type = type;
        this.error = error;
        this.message = "";
    }

    public Error(ErrorEnum type) {
        this.type = type;
        this.error = null;
        this.message = "";
    }

    public Error(ErrorEnum type, String message) {
        this.type = type;
        this.error = null;
        this.message = message;
    }

    public Error(ErrorEnum type, Throwable error, String message) {
        this.type = type;
        this.error = error;
        this.message = message;
    }

    public String toString() {
        return this.type.toString() +
                (this.error != null ? AmplificationHelper.LINE_SEPARATOR + this.getStackTrace.apply(this.error) : "") +
                (this.message.isEmpty() ? "" : AmplificationHelper.LINE_SEPARATOR + this.message);
    }

    private Function<Throwable, String> getStackTrace = throwable -> {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    };
}
