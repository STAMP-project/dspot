package eu.stamp_project.automaticbuilder;


import eu.stamp_project.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 14/07/17.
 */
public class AutomaticBuilderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticBuilderFactory.class);

    public static AutomaticBuilder getAutomaticBuilder(InputConfiguration configuration) {
        if (configuration.getBuilder() == null) {
            String builderType = configuration.getBuilderName();
            AutomaticBuilder automaticBuilder;
            if (builderType == null) {
                LOGGER.warn("No automatic builder specified in configuration, going to default.");
                LOGGER.info("Default: provided Maven automatic builder.");
                automaticBuilder = new MavenAutomaticBuilder();
            } else if (builderType.toUpperCase().contains("GRADLE")) {
                LOGGER.info("Selected Gradle automatic builder.");
                automaticBuilder = new GradleAutomaticBuilder(configuration);
            } else if (builderType.toUpperCase().contains("MAVEN")) {
                LOGGER.info("Selected Maven automatic builder.");
                automaticBuilder = new MavenAutomaticBuilder();
            } else {
                LOGGER.warn(builderType + ": unknown automatic builder specified in configuration, going to default.");
                LOGGER.info("Default: provided Maven automatic builder.");
                automaticBuilder = new MavenAutomaticBuilder();
            }
            configuration.setBuilder(automaticBuilder);
        }
        return configuration.getBuilder();
    }
}
