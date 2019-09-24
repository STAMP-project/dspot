package eu.stamp_project.utils.options.check;

import eu.stamp_project.Main;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.utils.options.SelectorEnum;
import eu.stamp_project.utils.options.InputConfiguration;
import eu.stamp_project.utils.report.error.Error;
import eu.stamp_project.utils.report.error.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/11/18
 */
public class Checker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Checker.class);

    /*
        Checking algorithms
     */
    public static void preChecking() {
        // project root is mandatory
        Checker.checkPathnameNotNullAndFileExist(
                InputConfiguration.get().getAbsolutePathToProjectRoot(),
                ErrorEnum.ERROR_PATH_TO_PROJECT_ROOT_PROPERTY,
                "You did not provide the path to the root folder of your project, which is mandatory.",
                "The provided path to the root folder of your project is incorrect, the folder does not exist."
        );
        // target module
        Checker.checkPathPropertyValue(
                InputConfiguration.get().getTargetModule(),
                ErrorEnum.ERROR_PATH_TO_TARGET_MODULE_PROPERTY,
                "targeted module", // TODO
                InputConfiguration.get().getAbsolutePathToProjectRoot()
        );

        // source folders: src and testSrc
        Checker.checkPathPropertyValue(
                InputConfiguration.get().getPathToSourceCode(),
                ErrorEnum.ERROR_PATH_TO_SRC_PROPERTY,
                "source folder", // TODO
                InputConfiguration.get().getPathToFolderToBeAmplified()
        );
        Checker.checkPathPropertyValue(
                InputConfiguration.get().getPathToTestSourceCode(),
                ErrorEnum.ERROR_PATH_TO_TEST_SRC_PROPERTY,
                "test source folder", // TODO
                InputConfiguration.get().getPathToFolderToBeAmplified()
        );

        // path to maven home
        Checker.checkPathPropertyValue(
                InputConfiguration.get().getMavenHome(),
                ErrorEnum.ERROR_PATH_TO_MAVEN_HOME,
                "maven installation", // TODO
                InputConfiguration.get().getPathToFolderToBeAmplified()
        );


        checkIsACorrectVersion(InputConfiguration.get().getDescartesVersion());
        checkIsACorrectVersion(InputConfiguration.get().getPitVersion());
        // TODO check JVM args and System args
        checkJVMArgs(InputConfiguration.get().getJVMArgs()); // no checks since it is a soft checks
        checkSystemProperties(InputConfiguration.get().getSystemProperties());
    }

    public static void postChecking() {
        // binary folders: classes and test-classes
        Checker.checkPathPropertyValue(
                InputConfiguration.get().getPathToClasses(),
                ErrorEnum.ERROR_PATH_TO_SRC_CLASSES_PROPERTY,
                "binaries folder", //
                InputConfiguration.get().getPathToFolderToBeAmplified()

        );
        Checker.checkPathPropertyValue(
                InputConfiguration.get().getPathToTestClasses(),
                ErrorEnum.ERROR_PATH_TO_TEST_CLASSES_PROPERTY,
                "test binaries folder", //
                InputConfiguration.get().getPathToFolderToBeAmplified()

        );
    }


    // TODO must be enhanced.
    /*
        For now, I'll "soft" check the JVM args until we find a proper way to do so.
        I'll restrict the check to jvmArgs=-Xmx2048m,-Xms1024m,-Dis.admin.user=admin,-Dis.admin.passwd=$2pRSid#,
        i.e increase the memory and gives some property
        By soft checks, I mean that DSpot won't throw errors (like others checks) but will display a warning.
        Same for checkSystemProperties
     */
    public static boolean checkJVMArgs(String jvmArgs) {
        final String[] jvmArgsArrays = jvmArgs.split(",");
        final Pattern memoryPattern = Pattern.compile("-Xm(x|s)(\\d+)(m|M|g|G)");
        boolean isOkayGlobal = true;
        for (int i = 0; i < jvmArgsArrays.length; i++) {
            final String currentArgs = jvmArgsArrays[i];
            if (currentArgs.isEmpty()) {
                continue;
            }
            boolean isOkay = memoryPattern.matcher(currentArgs).matches();
            isOkay |= currentArgs.startsWith("-D") && currentArgs.contains("=");
            if (!isOkay) {
                LOGGER.warn("You gave JVM args through properties file.");
                LOGGER.warn("DSpot could not recognize it: {}", currentArgs);
                LOGGER.warn("DSpot will continue because for now, it able to recognize memory options and properties.");
                LOGGER.warn("However, we advise you to double check them.");
                isOkayGlobal = false;
                // TODO should throws an error
            }
        }
        return isOkayGlobal;
    }

    public static boolean checkSystemProperties(String systemProperties) {
        return true;
    }

    public static void checkIsACorrectVersion(final String proposedVersion) {
       if (!Pattern.compile("(\\p{Digit})+(\\.(\\p{Digit})+)*(-SNAPSHOT)?").matcher(proposedVersion).matches()) {
            Main.GLOBAL_REPORT.addInputError(new Error(
                            ErrorEnum.ERROR_INVALID_VERSION, "Version " + proposedVersion + " is not a valid version"
                    )
            );
            throw new InputErrorException();
        }
    }

    private static void checkPathPropertyValue(final String propertyValue,
                                               final ErrorEnum errorEnumInCaseOfError,
                                               final String naturalLanguageDesignation,
                                               final String rootPathProject) {
        if (propertyValue != null) {
            final String additionalMessage = "The provided path to the " + naturalLanguageDesignation + " of your project is incorrect, the folder does not exist."
                    + AmplificationHelper.LINE_SEPARATOR + " This path should be either relative to the path pointed by " +
                    "--absolute-path-to-project-root command line options,"
                    + AmplificationHelper.LINE_SEPARATOR + "or an absolute path";
            if (new File(propertyValue).isAbsolute()) {
                Checker.checkFileExists(propertyValue, errorEnumInCaseOfError, additionalMessage);
            } else {
                Checker.checkFileExists(rootPathProject + "/" + propertyValue, errorEnumInCaseOfError, additionalMessage);
            }
        }
    }

    private static void checkPathnameNotNullAndFileExist(final String pathname,
                                                         ErrorEnum errorEnumInCaseOfError,
                                                         String additionalMessageWhenIsNull,
                                                         String additionalMessageWhenDoesNotExist) {
        if (pathname == null) {
            Main.GLOBAL_REPORT.addInputError(new Error(errorEnumInCaseOfError, additionalMessageWhenIsNull));
            throw new InputErrorException();
        } else {
            checkFileExists(pathname, errorEnumInCaseOfError, additionalMessageWhenDoesNotExist);
        }
    }

    private static void checkFileExists(final String pathname, ErrorEnum errorEnumInCaseOfError, String additionalMessage) {
        if (!new File(pathname).exists()) {
            Main.GLOBAL_REPORT.addInputError(new Error(errorEnumInCaseOfError, additionalMessage + "(" + pathname + ")"));
            throw new InputErrorException();
        }
    }
}

