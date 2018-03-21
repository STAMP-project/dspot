package fr.inria.stamp.demo.json;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/03/18
 */
public class MutantDataJSON {

    public final String fullQualifiedClassName;
    public final String methodName;
    public final int line;
    public final String status;

    public MutantDataJSON(String fullQualifiedClassName, String methodName, int line, String status) {
        this.fullQualifiedClassName = fullQualifiedClassName;
        this.methodName = methodName;
        this.line = line;
        this.status = status;
    }
}
