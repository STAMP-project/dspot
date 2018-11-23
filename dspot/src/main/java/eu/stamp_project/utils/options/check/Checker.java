package eu.stamp_project.utils.options.check;

import com.martiansoftware.jsap.JSAPResult;
import eu.stamp_project.Main;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.report.Error;
import eu.stamp_project.utils.report.ErrorEnum;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/11/18
 */
public class Checker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Checker.class);

    /*
        PATH FILE CHECK
     */

    public static void checkPathToPropertiesValue(JSAPResult jsapConfig) {
        boolean error = false;
        if (jsapConfig.getString("path-to-properties") == null) {
            LOGGER.error("There is a problem with the command line option path-to-properties.");
            Main.globalReport.addInputError(new Error(
                    ErrorEnum.ERROR_PATH_TO_PROPERTIES, "You did not provide the path to your properties file, which is mandatory."
            ));
            error = true;
        } else if (!new File(jsapConfig.getString("path-to-properties")).exists()) {
            LOGGER.error("There is a problem with the command line option path-to-properties.");
            Main.globalReport.addInputError(new Error(
                    ErrorEnum.ERROR_PATH_TO_PROPERTIES, "The provided path to the properties file is incorrect, the properties does not exist."
            ));
            error = true;
        }
        if (error) {
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
                Main.globalReport
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
