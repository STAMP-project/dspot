package fr.inria.diversify.utils;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static fr.inria.diversify.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/09/17
 */
public class Initializer {

	public static void initialize(InputConfiguration configuration)
			throws IOException, InterruptedException {
		InputProgram program = InputConfiguration.initInputProgram(configuration);
		program.setProgramDir(DSpotUtils.computeProgramDirectory.apply(configuration));
		configuration.setInputProgram(program);
		Initializer.initialize(configuration, program);
	}

	public static void initialize(InputConfiguration configuration, InputProgram program) {
		AutomaticBuilderFactory.reset();
		AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
		builder.compile(program.getProgramDir());
		if (!new File("target/dspot/dependencies/compare").exists()) {
			DSpotUtils.copyPackageFromResources("fr/inria/diversify/compare/",
					"MethodsHandler", "ObjectLog", "Observation", "Utils");
		}
	}

	public static void compileTest(InputConfiguration configuration) {
		InputProgram program = configuration.getInputProgram();
		String dependencies = AutomaticBuilderFactory.getAutomaticBuilder(configuration)
				.buildClasspath(program.getProgramDir());
		dependencies += PATH_SEPARATOR + "target/dspot/dependencies/";
		File output = new File(program.getProgramDir() + "/" + program.getClassesDir());
		File outputTest = new File(program.getProgramDir() + "/" + program.getTestClassesDir());
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
