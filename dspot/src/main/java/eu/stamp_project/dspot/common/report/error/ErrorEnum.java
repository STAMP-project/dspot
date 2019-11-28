package eu.stamp_project.dspot.common.report.error;

import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;

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
    ERROR_PATH_TO_PROJECT_ROOT_PROPERTY("project root", "--absolute-path-to-project-root"),
    ERROR_PATH_TO_TARGET_MODULE_PROPERTY("targeted module",  "--target-module"),
    ERROR_PATH_TO_SRC_PROPERTY("source folder", "--relative-path-to-source-code"),
    ERROR_PATH_TO_TEST_SRC_PROPERTY("test source folder", "--relative-path-to-test-code"),
    ERROR_PATH_TO_SRC_CLASSES_PROPERTY("binaries folder", "--relative-path-to-classes"),
    ERROR_PATH_TO_TEST_CLASSES_PROPERTY("test binaries folder", "--relative-path-to-test-classes"),
    ERROR_PATH_TO_SECOND_VERSION("path to second version", "--absolute-path-to-second-version"),
    ERROR_PATH_TO_MAVEN_HOME("maven installation", "--maven-home"),
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
            "This is usually due to the value of the command line option --pit-filter-classes-to-keep."
            + AmplificationHelper.LINE_SEPARATOR +
            "Specify the filter of classes to keep used by PIT. " +
            "This allow you restrict the scope of the mutation done by PIT. " +
            "If this is not specified, DSpot will try to build on the " +
            "fly a filter that takes into account the largest number of classes, e.g. the topest package. "
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

    ErrorEnum(String naturalDesignation, String name) {
        this.message =" There is a problem with the provided path to the "
                + naturalDesignation + "(" + name + " command-line)";
    }
}
