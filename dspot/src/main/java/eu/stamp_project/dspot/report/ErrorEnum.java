package eu.stamp_project.dspot.report;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public enum ErrorEnum {

    ERROR_PRE_SELECTION("Something bad happened during selection before amplification."),
    ERROR_INPUT_AMPLIFICATION("Something bad happened during input amplification."),
    ERROR_ASSERT_AMPLIFICATION("Something bad happened during assertion amplification"),
    ERROR_SELECTION("Something bad happened during selection to keep amplified tests (post-amplification).");

    private String message;

    public String getMessage() {
        return message;
    }

    ErrorEnum(String message) {
        this.message = message;
    }

}
