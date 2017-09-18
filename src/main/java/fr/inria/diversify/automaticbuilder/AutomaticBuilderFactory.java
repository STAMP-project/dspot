package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.Log;

import java.util.function.Predicate;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 14/07/17.
 */
public class AutomaticBuilderFactory {

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
				Log.warn("No automatic builder specified in configuration, going to default.");
				Log.debug("Default: provided Maven automatic builder.");
				automaticBuilder = new MavenAutomaticBuilder(configuration);
			} else if (builderType.toUpperCase().contains("GRADLE")) {
				Log.debug("Selected Gradle automatic builder.");
				automaticBuilder = new GradleAutomaticBuilder(configuration);
			} else if (builderType.toUpperCase().contains("MAVEN")) {
				Log.debug("Selected Maven automatic builder.");
				automaticBuilder = new MavenAutomaticBuilder(configuration);
			} else {
				Log.warn(builderType + ": unknown automatic builder specified in configuration, going to default.");
				Log.debug("Default: provided Maven automatic builder.");
				automaticBuilder = new MavenAutomaticBuilder(configuration);
			}
		}
		return automaticBuilder;
	}
}
