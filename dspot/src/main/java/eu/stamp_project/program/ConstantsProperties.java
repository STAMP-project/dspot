package eu.stamp_project.program;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/06/18
 */
public class ConstantsProperties {

    public static final InputConfigurationProperty PROJECT_ROOT_PATH =
            new InputConfigurationProperty(
                    "project",
                    "specify the path to the root of the project. " +
                            "This path can be either absolute (recommended) or relative to the working directory of the DSpot process. " +
                            "We consider as root of the project folder that contain the top-most parent in a multi-module project.",
                    null
            );

    public static final InputConfigurationProperty MODULE =
            new InputConfigurationProperty(
                    "targetModule",
                    "specify the module to be amplified. This value must be a relative path from the property " + PROJECT_ROOT_PATH.getName() + ". " +
                            "If your project is multi-module, you must use this property because DSpot works at module level.",
                    ""
            );

    public static final InputConfigurationProperty SRC_CODE =
            new InputConfigurationProperty(
                    "src",
                    "specify the relative path from " +
                            PROJECT_ROOT_PATH.getName() + "/" + MODULE.getName() +
                            " of the folder that contain sources (.java)",
                    "src/main/java/"
            );

    public static final InputConfigurationProperty TEST_SRC_CODE =
            new InputConfigurationProperty(
                    "testSrc",
                    "specify the relative path from " +
                            PROJECT_ROOT_PATH.getName() + "/" + MODULE.getName() +
                            " of the folder that contain test sources (.java)",
                    "src/test/java/"
            );

    public static final InputConfigurationProperty SRC_CLASSES =
            new InputConfigurationProperty(
                    "classes",
                    "specify the relative path from " +
                            PROJECT_ROOT_PATH.getName() + "/" + MODULE.getName() +
                            " of the folder that contain binaries of the source program (.class)",
                    "target/classes/"
            );

    public static final InputConfigurationProperty TEST_CLASSES =
            new InputConfigurationProperty(
                    "testclasses",
                    "specify the relative path from " +
                            PROJECT_ROOT_PATH.getName() + "/" + MODULE.getName() +
                            " of the folder that contain binaries of the test source program (.class)",
                    "target/test-classes/"
            );

    public static final InputConfigurationProperty ADDITIONAL_CP_ELEMENTS =
            new InputConfigurationProperty(
                    "additionalClasspathElements",
                    "",//TODO relative to the root, separated with ,
                    ""
            );

    public static final InputConfigurationProperty SYSTEM_PROPERTIES =
            new InputConfigurationProperty(
                    "systemProperties",
                    "",//TODO
                    ""
            );

    public static final InputConfigurationProperty PATH_TO_SECOND_VERSION =
            new InputConfigurationProperty(
                    "folderPath",
                    "",//TODO
                    ""
            );

    public static final InputConfigurationProperty AUTOMATIC_BUILDER_NAME =
            new InputConfigurationProperty(
                    "automaticBuilderName",
                    "",//TODO
                    ""
            );

    public static final InputConfigurationProperty OUTPUT_DIRECTORY =
            new InputConfigurationProperty(
                    "outputDirectory",
                    "", // TODO
                    "target/dspot/output"
            );

    public static final InputConfigurationProperty MAVEN_HOME =
            new InputConfigurationProperty(
                    "maven.home",
                    "", // TODO
                    ""
            );

    public static final InputConfigurationProperty DELTA_ASSERTS_FLOAT =
            new InputConfigurationProperty(
                    "delta",
                    "",//TODO
                    "0.1"
            );

    public static final InputConfigurationProperty FILTER =
            new InputConfigurationProperty(
                    "filter",
                    "",//TODO
                    ""
            );

    public static final InputConfigurationProperty PIT_VERSION =
            new InputConfigurationProperty(
                    "pitVersion",
                    "", //TODO
                    "1.3.0"
            );

    public static final InputConfigurationProperty DESCARTES_VERSION =
            new InputConfigurationProperty(
                    "descartesVersion",
                    "", //TODO
                    "1.2"
            );

    public static final InputConfigurationProperty BASE_SHA =
            new InputConfigurationProperty(
                    "baseSha",
                    "", //TODO
                    ""
            );

    public static final InputConfigurationProperty EXCLUDED_CLASSES =
            new InputConfigurationProperty(
                    "excludedClasses",
                    "", //TODO separated with ,
                    ""
            );

    public static final InputConfigurationProperty EXCLUDED_TEST_CASES =
            new InputConfigurationProperty(
                    "excludedTestCases",
                    "", //TODO
                    ""
            );

    public static final InputConfigurationProperty TIMEOUT_PIT =
            new InputConfigurationProperty(
                    "pitTimeout",
                    "", // TODO
                    ""
            );

    public static final InputConfigurationProperty JVM_ARGS =
            new InputConfigurationProperty(
                    "jvmArgs",
                    "", // TODO
                    ""
            );

    public static final InputConfigurationProperty DESCARTES_MUTATORS =
            new InputConfigurationProperty(
                    "descartesMutators",
                    "",//TODO
                    ""
            );
}
