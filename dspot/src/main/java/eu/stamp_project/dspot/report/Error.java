package eu.stamp_project.dspot.report;

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

    public Error(ErrorEnum type, Throwable error) {
        this.type = type;
        this.error = error;
    }

    public String toString() {
        return this.type.toString() + AmplificationHelper.LINE_SEPARATOR + this.getStackTrace.apply(this.error);
    }

    private Function<Throwable, String> getStackTrace = throwable -> {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    };
}
