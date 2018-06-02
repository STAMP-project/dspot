package eu.stamp_project.utils;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.sosiefier.InputConfiguration;

import java.io.IOException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/09/17
 */
public class Initializer {

	public static void initialize(InputConfiguration configuration)
			throws IOException, InterruptedException {
		AutomaticBuilderFactory.reset();
		AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
		builder.compile();
		DSpotUtils.copyPackageFromResources();
	}

}
