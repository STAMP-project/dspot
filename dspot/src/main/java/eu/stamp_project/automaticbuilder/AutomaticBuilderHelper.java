package eu.stamp_project.automaticbuilder;

import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtPackage;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/11/18
 */
public class AutomaticBuilderHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticBuilderHelper.class);

    private static final String MESSAGE_WARN_PIT_NO_FILTER = "You gave an empty filter. To use PIT, it is recommend to specify a filter, at least, the top package of your program, otherwise, PIT may take a long time or could not be run.";

    public static String getFilter() {
        if (InputConfiguration.get().getFilter().isEmpty()) {
            LOGGER.warn(MESSAGE_WARN_PIT_NO_FILTER);
            LOGGER.warn("Trying to compute the top package of the project...");
            CtPackage currentPackage = InputConfiguration.get().getFactory().Package().getRootPackage();
            while (currentPackage.getTypes().isEmpty()) {
                currentPackage = (CtPackage) currentPackage.getPackages().toArray()[0];
            }
            InputConfiguration.get().setFilter(currentPackage.getQualifiedName() + ".*");
        }
        return InputConfiguration.get().getFilter();
    }
}
