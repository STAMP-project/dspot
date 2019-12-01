package eu.stamp_project.dspot.common.configuration.check;

import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.report.error.Error;
import eu.stamp_project.dspot.common.report.error.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Pattern;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/11/18
 */
public class Checker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Checker.class);

    /*
        Checking algo
     */

    public static void preChecking(UserInput configuration) {
        // project root is mandatory

        Checker.checkPathnameNotNullAndFileExist(
                configuration.getAbsolutePathToTopProjectRoot(),
                ErrorEnum.ERROR_PATH_TO_PROJECT_ROOT_PROPERTY,
                "You did not provide the path to the root folder of your project, which is mandatory.",
                "The provided path to the root folder of your project is incorrect, the folder does not exist."
        );
        String currentPath = DSpotUtils.shouldAddSeparator.apply(configuration.getAbsolutePathToTopProjectRoot());
        configuration.setAbsolutePathToProjectRoot(currentPath);
        // target module
        final String targetModulePropertyValue = DSpotUtils.shouldAddSeparator.apply(configuration.getTargetModule());
        Checker.checkPathPropertyValue(
                targetModulePropertyValue,
                ErrorEnum.ERROR_PATH_TO_TARGET_MODULE_PROPERTY,
                "targeted module",
                currentPath
        );
        configuration.setTargetModule(targetModulePropertyValue);
        currentPath += targetModulePropertyValue != null ? targetModulePropertyValue : "";

        // source folders: src and testSrc
        Checker.checkPathPropertyValue(
                configuration.getPathToSourceCode(),
                ErrorEnum.ERROR_PATH_TO_SRC_PROPERTY,
                "source folder",
                currentPath
        );
        Checker.checkPathPropertyValue(
                configuration.getPathToTestSourceCode(),
                ErrorEnum.ERROR_PATH_TO_TEST_SRC_PROPERTY,
                "test source folder",
                currentPath
        );

        // path to maven home
        Checker.checkPathPropertyValue(
                configuration.getMavenHome(),
                ErrorEnum.ERROR_PATH_TO_MAVEN_HOME,
                "maven installation",
                currentPath
        );


        checkIsACorrectVersion(configuration.getPitVersion());
        checkIsACorrectVersion(configuration.getDescartesVersion());
        // TODO check JVM args and System args
        checkJVMArgs(configuration.getJVMArgs()); // no checks since it is a soft checks
        checkSystemProperties(configuration.getSystemProperties());
    }

    public static void postChecking(UserInput configuration) {
        // we check now the binaries folders after the compilation
        String currentPath = DSpotUtils.shouldAddSeparator.apply(configuration.getAbsolutePathToProjectRoot());

        // binary folders: classes and test-classes
        Checker.checkPathPropertyValue(
                configuration.getPathToClasses(),
                ErrorEnum.ERROR_PATH_TO_SRC_CLASSES_PROPERTY,
                "binaries folder",
                currentPath
        );
        Checker.checkPathPropertyValue(
                configuration.getPathToClasses(),
                ErrorEnum.ERROR_PATH_TO_TEST_CLASSES_PROPERTY,
                "test binaries folder",
                currentPath
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
            DSpotState.GLOBAL_REPORT.addInputError(new Error(
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
                    + AmplificationHelper.LINE_SEPARATOR + " This path should be either relative to the path pointed by "
                    + " --absolute-path-to-project-root command line option"
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
            DSpotState.GLOBAL_REPORT.addInputError(new Error(errorEnumInCaseOfError, additionalMessageWhenIsNull));
            throw new InputErrorException();
        } else {
            checkFileExists(pathname, errorEnumInCaseOfError, additionalMessageWhenDoesNotExist);
        }
    }

    private static void checkFileExists(final String pathname, ErrorEnum errorEnumInCaseOfError, String additionalMessage) {
        if (!new File(pathname).exists()) {
            DSpotState.GLOBAL_REPORT.addInputError(new Error(errorEnumInCaseOfError, additionalMessage + "(" + pathname + ")"));
            throw new InputErrorException();
        }
    }
}

