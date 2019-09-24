package eu.stamp_project.utils.report.error;

import eu.stamp_project.utils.AmplificationHelper;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public enum ErrorEnum {

    /*
        ERRORS INPUT
     */
    ERROR_PATH_TO_PROJECT_ROOT_PROPERTY("There is a problem with the provided path to the root folder of the project."),
    ERROR_PATH_TO_TARGET_MODULE_PROPERTY("There is a problem with the provided path to the targeted module."),
    ERROR_PATH_TO_SRC_PROPERTY("There is a problem with the provided path to the source folder of the project."),
    ERROR_PATH_TO_TEST_SRC_PROPERTY("There is a problem with the provided path to the test source folder of the project."),
    ERROR_PATH_TO_SRC_CLASSES_PROPERTY("There is a problem with the provided path to the classes folder of the project."),
    ERROR_PATH_TO_TEST_CLASSES_PROPERTY("There is a problem with the provided path to the test classes folder of the project."),
    ERROR_PATH_TO_SECOND_VERSION("There is a problem with the provided path to the root folder of the second version of the project."),
    ERROR_PATH_TO_MAVEN_HOME("There is a problem with the provided path to the maven home."),
    ERROR_INVALID_VERSION("Invalid version"),

    /*
        ERRORS BEFORE AMPLIFICATION PROCESS
     */
    ERROR_NO_TEST_COULD_BE_FOUND_MATCHING_REGEX("Could not find any test class that match the given regular expression."),
    ERROR_NO_TEST_COULD_BE_FOUND("Could not find any test class that match at least one of the given regular expression."),

    /*
        ERRORS DURING AMPLIFICATION PROCESS
    */
    ERROR_EXEC_TEST_BEFORE_AMPLIFICATION("Something bad happened when DSpot tried to execute the tests before starting the amplification."),
    ERROR_PRE_SELECTION("Something bad happened during selection before amplification."),
    ERROR_INPUT_AMPLIFICATION("Something bad happened during input amplification."),
    ERROR_ASSERT_AMPLIFICATION("Something bad happened during assertion amplification"),
    ERROR_SELECTION("Something bad happened during selection to keep amplified tests (post-amplification)."),

     /*
        ERRORS DUE TO I/O
     */
     ERROR_PRINT_USING_TO_STRING("Something bad happened when DSpot tried to print a test class using the toString()"),

    /*
        ERRORS LINKED TO A SPECIFIC IMPLEMENTATION
    */

    //  PitMutantScoreSelector
    ERROR_ORIGINAL_MUTATION_SCORE("Something bad happened when DSpot tried to computed the original mutation score."
            + AmplificationHelper.LINE_SEPARATOR +
            "This is usually due to the value of the command line --path-pit-result."
            + AmplificationHelper.LINE_SEPARATOR +
            "This is can be also due to a specific configuration of your test suite. If any test fails,"
            + AmplificationHelper.LINE_SEPARATOR +
            "PIT (and so DSpot) won't be able to be executed. Please, check your environment variables,"
            + AmplificationHelper.LINE_SEPARATOR +
            "external files, etc. You can use --excluded-classes and --excluded-test-cases"
            + AmplificationHelper.LINE_SEPARATOR +
            " to exclude respectively specific test classes ans test cases."
    );

    private String message;

    public String getMessage() {
        return message;
    }

    ErrorEnum(String message) {
        this.message = message;
    }

}
