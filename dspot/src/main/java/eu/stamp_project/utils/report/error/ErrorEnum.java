package eu.stamp_project.utils.report;

import eu.stamp_project.utils.program.ConstantsProperties;
import eu.stamp_project.utils.program.InputConfigurationProperty;

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
        ERRORS PROPERTIES
     */
    ERROR_PATH_TO_PROJECT_ROOT_PROPERTY(ConstantsProperties.PROJECT_ROOT_PATH),
    ERROR_PATH_TO_TARGET_MODULE_PROPERTY(ConstantsProperties.MODULE),
    ERROR_PATH_TO_SRC_PROPERTY(ConstantsProperties.SRC_CODE),
    ERROR_PATH_TO_TEST_SRC_PROPERTY(ConstantsProperties.TEST_SRC_CODE),
    ERROR_PATH_TO_SRC_CLASSES_PROPERTY(ConstantsProperties.SRC_CLASSES),
    ERROR_PATH_TO_TEST_CLASSES_PROPERTY(ConstantsProperties.TEST_CLASSES),
    ERROR_PATH_TO_SECOND_VERSION(ConstantsProperties.PATH_TO_SECOND_VERSION),
    ERROR_PATH_TO_MAVEN_HOME(ConstantsProperties.MAVEN_HOME),
    ERROR_INVALID_VERSION("Invalid version"),

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

    ErrorEnum(InputConfigurationProperty property) {
        this.message =
                "There is a problem with the provided path to the "
                        + property.getNaturalLanguageDesignation() + "(" + property.getName() + " property)";
    }
}
