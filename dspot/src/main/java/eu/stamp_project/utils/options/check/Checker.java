package eu.stamp_project.utils.options.check;

import com.martiansoftware.jsap.JSAPResult;
import eu.stamp_project.Main;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.ConstantsProperties;
import eu.stamp_project.utils.report.error.Error;
import eu.stamp_project.utils.report.error.ErrorEnum;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/11/18
 */
public class Checker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Checker.class);

    /*
        PROPERTIES CHECK
     */

    public static void checkBinariesFolders(Properties properties) {

        String currentPath = DSpotUtils.shouldAddSeparator.apply(properties.getProperty(ConstantsProperties.PROJECT_ROOT_PATH.getName()));
        final String targetModulePropertyValue = DSpotUtils.shouldAddSeparator.apply(properties.getProperty(ConstantsProperties.MODULE.getName()));
        currentPath += targetModulePropertyValue != null ? targetModulePropertyValue : "";

        // binary folders: classes and test-classes
        Checker.checkRelativePathPropertyValue(
                properties.getProperty(ConstantsProperties.SRC_CLASSES.getName()),
                ErrorEnum.ERROR_PATH_TO_SRC_CLASSES_PROPERTY,
                ConstantsProperties.SRC_CLASSES.getNaturalLanguageDesignation(),
                currentPath
        );
        Checker.checkRelativePathPropertyValue(
                properties.getProperty(ConstantsProperties.TEST_CLASSES.getName()),
                ErrorEnum.ERROR_PATH_TO_TEST_CLASSES_PROPERTY,
                ConstantsProperties.TEST_CLASSES.getNaturalLanguageDesignation(),
                currentPath
        );
    }

    public static void checkProperties(Properties properties) {
        // project root is mandatory
        String currentPath = DSpotUtils.shouldAddSeparator.apply(properties.getProperty(ConstantsProperties.PROJECT_ROOT_PATH.getName()));
        Checker.checkPathnameNotNullAndFileExist(
                currentPath,
                ErrorEnum.ERROR_PATH_TO_PROJECT_ROOT_PROPERTY,
                "You did not provide the path to the root folder of your project, which is mandatory.",
                "The provided path to the root folder of your project is incorrect, the folder does not exist."
        );
        // target module
        final String targetModulePropertyValue = DSpotUtils.shouldAddSeparator.apply(properties.getProperty(ConstantsProperties.MODULE.getName()));
        Checker.checkRelativePathPropertyValue(
                targetModulePropertyValue,
                ErrorEnum.ERROR_PATH_TO_TARGET_MODULE_PROPERTY,
                ConstantsProperties.MODULE.getNaturalLanguageDesignation(),
                currentPath
        );
        currentPath += targetModulePropertyValue != null ? targetModulePropertyValue : "";

        // source folders: src and testSrc
        Checker.checkRelativePathPropertyValue(
                properties.getProperty(ConstantsProperties.SRC_CODE.getName()),
                ErrorEnum.ERROR_PATH_TO_SRC_PROPERTY,
                ConstantsProperties.SRC_CODE.getNaturalLanguageDesignation(),
                currentPath
        );
        Checker.checkRelativePathPropertyValue(
                properties.getProperty(ConstantsProperties.TEST_SRC_CODE.getName()),
                ErrorEnum.ERROR_PATH_TO_TEST_SRC_PROPERTY,
                ConstantsProperties.TEST_SRC_CODE.getNaturalLanguageDesignation(),
                currentPath
        );

        // path to maven home
        Checker.checkRelativePathPropertyValue(
                properties.getProperty(ConstantsProperties.MAVEN_HOME.getName()),
                ErrorEnum.ERROR_PATH_TO_MAVEN_HOME,
                ConstantsProperties.MAVEN_HOME.getNaturalLanguageDesignation(),
                currentPath
        );

        if (properties.getProperty(ConstantsProperties.DESCARTES_VERSION.getName()) != null) {
            checkIsACorrectVersion(properties.getProperty(ConstantsProperties.DESCARTES_VERSION.getName()));
        }
        if (properties.getProperty(ConstantsProperties.PIT_VERSION.getName()) != null) {
            checkIsACorrectVersion(properties.getProperty(ConstantsProperties.PIT_VERSION.getName()));
        }
        // TODO check JVM args and System args
        checkJVMArgs(ConstantsProperties.JVM_ARGS.get(properties)); // no checks since it is a soft checks
        Checker.checkProperties(ConstantsProperties.SYSTEM_PROPERTIES.get(properties));
    }

    // TODO must be enhanced.
    /*
        For now, I'll "soft" check the JVM args until we find a proper way to do so.
        I'll restrict the check to jvmArgs=-Xmx2048m,-Xms1024m,-Dis.admin.user=admin,-Dis.admin.passwd=$2pRSid#,
        i.e increase the memory and gives some property
        By soft checks, I mean that DSpot won't throw errors (like others checks) but will display a warning.
        Same for checkProperties
     */
    public static boolean checkJVMArgs(String jvmArgs) {
        final String[] jvmArgsArrays = jvmArgs.split(",");
        final Pattern memoryPattern = Pattern.compile("-Xm(x|s)(\\d+)(m|M|g|G)");
        boolean isOkayGlobal = true;
        for (int i = 0; i < jvmArgsArrays.length; i++) {
            final String currentArgs = jvmArgsArrays[i];
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

    public static boolean checkProperties(String systemProperties) {
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

    private static void checkRelativePathPropertyValue(final String propertyValue,
                                                       final ErrorEnum errorEnumInCaseOfError,
                                                       final String naturalLanguageDesignation,
                                                       final String rootPathProject) {
        if (propertyValue != null) {
            final String additionalMessage = "The provided path to the " + naturalLanguageDesignation + " of your project is incorrect, the folder does not exist."
                    + AmplificationHelper.LINE_SEPARATOR + " This path should be relative to the path pointed by "
                    + ConstantsProperties.PROJECT_ROOT_PATH.getName() + " property.";
            Checker.checkFileExists(rootPathProject + "/" + propertyValue, errorEnumInCaseOfError, additionalMessage);
        }
    }

    /*
        PROPERTIES PATH FILE CHECK
     */
    public static void checkPathToPropertiesValue(JSAPResult jsapConfig) {
        Checker.checkPathnameNotNullAndFileExist(
                jsapConfig.getString("path-to-properties"),
                ErrorEnum.ERROR_PATH_TO_PROPERTIES,
                "You did not provide the path to your properties file, which is mandatory.",
                "The provided path to the properties file is incorrect, the properties file does not exist."
        );
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

    /*
        ENUM CHECK
     */

    public static void checkEnum(Class<?> enumClass, String value, String option) {
        final List<String> values = new ArrayList<>();
        values.add(value);
        checkEnum(enumClass, values, option);
    }

    public static void checkEnum(Class<?> enumClass, List<String> values, String option) {
        final ArrayList<String> copyValues = new ArrayList<>(values);
        if (!Checker.checkEnumAndRemoveIfIncorrect(enumClass, values)) {
            LOGGER.error("Any given value for {} match {}", option, enumClass.getName());
            LOGGER.error("{}", getPossibleValuesAsString(enumClass));
            LOGGER.error("DSpot will stop here, please checkEnum your input:");
            LOGGER.error("{}", String.join(AmplificationHelper.LINE_SEPARATOR + Checker.indentation, copyValues));
            throw new InputErrorException();
        }
    }

    private static final String indentation = "\t\t\t\t\t\t\t\t\t- ";

    private static String getPossibleValuesAsString(Class<?> enumClass) {
        return getPossibleValues(enumClass)
                .stream()
                .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR + Checker.indentation));
    }

    private static boolean checkEnumAndRemoveIfIncorrect(Class<?> enumClass, List<String> values) {
        boolean atLeastOneOptionIsOk = false;
        final List<String> possibleValues = getPossibleValues(enumClass);
        final ArrayList<String> copyValues = new ArrayList<>(values);
        for (String value : copyValues) {
            if (!possibleValues.contains(value)) {
                Main.GLOBAL_REPORT
                        .addInputError(
                                new Error(ErrorEnum.ERROR_NO_ENUM_VALUE_CORRESPOND_TO_GIVEN_INPUT, Checker.toString(enumClass, value))
                        );
                values.remove(value);
            } else {
                atLeastOneOptionIsOk = true;
            }
        }
        return atLeastOneOptionIsOk;
    }

    private static String toString(Class<?> enumClass, String wrongValue) {
        return enumClass.getName() + " does not have corresponding value to " + wrongValue;
    }

    @NotNull
    public static List<String> getPossibleValues(Class<?> enumClass) {
        return Arrays.stream(enumClass.getFields())
                .filter(field -> enumClass.equals(field.getType()))
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}

