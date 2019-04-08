package eu.stamp_project.automaticbuilder;

import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

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
            CtPackage currentPackage = InputConfiguration.get().getFactory().Package().getRootPackage();
            StringBuilder filter = new StringBuilder();
            if (!currentPackage.getTypes().isEmpty()) {
                LOGGER.warn("There is a class in the default package:");
                currentPackage.getTypes().stream()
                        .map(CtType::getQualifiedName)
                        .peek(type -> LOGGER.warn("\t- {}", type))
                        .forEach(type -> filter.append(type).append(","));
            }
            currentPackage = (CtPackage) currentPackage.getPackages().toArray()[0];
            while (currentPackage.getTypes().isEmpty()) {
                currentPackage = (CtPackage) currentPackage.getPackages().toArray()[0];
            }
            filter.append(currentPackage.getQualifiedName()).append(".*");
            LOGGER.info("A new filter has been computed on the fly: {}", filter.toString());
            InputConfiguration.get().setFilter(filter.toString());
        }
        return InputConfiguration.get().getFilter();
    }
}
