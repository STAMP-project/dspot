package eu.stamp_project.utils.report;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public enum ErrorEnum {

    /*
        ERRORS INPUT
     */
    ERROR_NO_ENUM_VALUE_CORRESPOND_TO_GIVEN_INPUT("The given value does not match with any value of the corresponding options."),
    ERROR_PATH_TO_PROPERTIES("There is a problem with the provided path to the properties file."),

    /*
        ERRORS BEFORE AMPLIFICATION PROCESS
     */
    ERROR_NO_TEST_COULD_BE_FOUND_MATCHING_REGEX("Could not find any test class that match the given regular expression."),
    ERROR_NO_TEST_COULD_BE_FOUND("Could not find any test class that match at least one of the given regular expression."),

    /*
        ERRORS DURING AMPLIFICATION PROCESS
     */
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
