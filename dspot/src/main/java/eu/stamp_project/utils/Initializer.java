package eu.stamp_project.utils;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import eu.stamp_project.utils.sosiefier.InputProgram;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/09/17
 */
public class Initializer {

	public static void initialize(InputConfiguration configuration)
			throws IOException, InterruptedException {
		InputProgram program = InputConfiguration.initInputProgram(configuration);
		configuration.setAbsolutePathToProjectRoot(DSpotUtils.computeProgramDirectory.apply(configuration));
		program.setProgramDir(DSpotUtils.computeProgramDirectory.apply(configuration));
		configuration.setInputProgram(program);
		Initializer.initialize(configuration, program);
	}

	public static void initialize(InputConfiguration configuration, InputProgram program) {
		AutomaticBuilderFactory.reset();
		AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
		builder.compile(configuration.getAbsolutePathToProjectRoot());
		DSpotUtils.copyPackageFromResources();
	}

	@Deprecated
	public static void compileTest(InputConfiguration configuration) {
		InputProgram program = configuration.getInputProgram();
		String dependencies = AutomaticBuilderFactory.getAutomaticBuilder(configuration)
				.buildClasspath(configuration.getAbsolutePathToProjectRoot());
		dependencies += PATH_SEPARATOR + "target/dspot/dependencies/";
		File output = new File(configuration.getAbsolutePathToProjectRoot() + "/" + program.getClassesDir());
		File outputTest = new File(configuration.getAbsolutePathToProjectRoot() + "/" + program.getTestClassesDir());
		try {
			FileUtils.cleanDirectory(outputTest);
		} catch (Exception ignored) {
			//the target directory does not exist, do not need to clean it
		}
		boolean statusTest = DSpotCompiler.compile(program.getAbsoluteTestSourceCodeDir(),
				output.getAbsolutePath() + PATH_SEPARATOR + dependencies, outputTest);
		if (!statusTest) {
			throw new RuntimeException("Error during compilation");
		}
	}





}
