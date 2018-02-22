package fr.inria.diversify.automaticbuilder;


import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 14/07/17.
 */
public class AutomaticBuilderFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticBuilderFactory.class);

	private static AutomaticBuilder automaticBuilder = null;

	public static void reset() {
		if (AutomaticBuilderFactory.automaticBuilder != null) {
			AutomaticBuilderFactory.automaticBuilder.reset();
			AutomaticBuilderFactory.automaticBuilder = null;
		}
	}

	public static AutomaticBuilder getAutomaticBuilder(InputConfiguration configuration) {
		if (automaticBuilder == null) {
			String builderType = configuration.getProperty("automaticBuilderName");
			if (builderType == null) {
				LOGGER.warn("No automatic builder specified in configuration, going to default.");
				LOGGER.info("Default: provided Maven automatic builder.");
				automaticBuilder = new MavenAutomaticBuilder(configuration);
			} else if (builderType.toUpperCase().contains("GRADLE")) {
				LOGGER.info("Selected Gradle automatic builder.");
				automaticBuilder = new GradleAutomaticBuilder(configuration);
			} else if (builderType.toUpperCase().contains("MAVEN")) {
				LOGGER.info("Selected Maven automatic builder.");
				automaticBuilder = new MavenAutomaticBuilder(configuration);
			} else {
				LOGGER.warn(builderType + ": unknown automatic builder specified in configuration, going to default.");
				LOGGER.info("Default: provided Maven automatic builder.");
				automaticBuilder = new MavenAutomaticBuilder(configuration);
			}
		}
		return automaticBuilder;
	}
}
