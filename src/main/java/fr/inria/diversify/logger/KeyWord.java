package fr.inria.diversify.logger;

/**
 * User: Simon
 * Date: 16/06/15
 * Time: 13:33
 */
public class KeyWord {
    public static final String separator = ":':";
    public static final String simpleSeparator = ";";
    public static final String endLine = "$$";

    public static final String variableObservation = "V";
    public static final String branchObservation = "B";
    public static final String methodCallObservation = "M";
    public static final String assertObservation = "AO";
    public static final String assertBefore = "AB";
    public static final String assertAfter = "AA";
    public static final String testStartObservation = "TS";
    public static final String testEndObservation = "TE";
    public static final String catchObservation = "C";
    public static final String throwObservation = "T";
    public static final String statementLog = "S";

    //for class observation
    public static final String getterKeyWord = "Gt";
    public static final String classKeyWord = "Cl";

    public static final String methodCallPrimitiveParameter = "MPP";
    public static final String methodCallCollectionParameter = "MCP";
    public static final String methodCallMapParameter = "MMP";

    public static final String methodCallObjectParameter = "MOP";
    public static final String ObjectParameterField = "OPF";
    public static final String methodCallMethod = "MM";
    public static final String methodCallReceiverType = "MR";

    public static final String primitiveKeyWord = "P";

    public static final char mapType = 'M';
    public static final char collectionType = 'C';
    public static final char objectType = 'O';
    public static final char primitiveType = 'P';

}
