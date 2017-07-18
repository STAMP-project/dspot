package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.Log;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 14/07/17.
 */
public class AutomaticBuilderFactory {

    public AutomaticBuilder getAutomaticBuilder(InputConfiguration configuration) {
        String builderType = configuration.getProperty("automaticBuilderName");
        if (builderType == null) {
            Log.warn("No automatic builder specified in configuration, going to default.");
            Log.debug("Default: provided Maven automatic builder.");
            return new MavenAutomaticBuilder(configuration);
        }
        if (builderType.toUpperCase().contains("GRADLE")) {
            Log.debug("Selected Gradle automatic builder.");
            return new GradleAutomaticBuilder();
        }
        if (builderType.toUpperCase().contains("MAVEN")) {
            Log.debug("Selected Maven automatic builder.");
            return new MavenAutomaticBuilder(configuration);
        }
        Log.warn(builderType + ": unknown automatic builder specified in configuration, going to default.");
        Log.debug("Default: provided Maven automatic builder.");
        return new MavenAutomaticBuilder(configuration);
    }

}