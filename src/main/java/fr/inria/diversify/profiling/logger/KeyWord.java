package fr.inria.diversify.profiling.logger;

/**
 * User: Simon
 * Date: 16/06/15
 * Time: 13:33
 */
public class KeyWord {
    public static final String separator = ":':";
    public static final String simpleSeparator = ";";
    public static final String endLine = "$$\n";

    public static final String variableObservation = "V";
    public static final String branchObservation = "P";
    public static final String methodCallObservation = "M";
    public static final String assertObservation = "As";
    public static final String testStartObservation = "TS";
    public static final String testEndObservation = "TE";
    public static final String catchObservation = "C";
    public static final String throwObservation = "T";
    public static final String logTransformation = "LT";

    //for class observation
    public static final String getterKeyWord = "Gt";
    public static final String classKeyWord = "Cl";

}
