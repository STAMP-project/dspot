package fr.inria.diversify;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.IOException;
import java.util.Set;


/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:16
 */
public class Utils {

	private static InputProgram inputProgram;
	private static InputConfiguration inputConfiguration;
	private static DSpotCompiler compiler;

	private static String currentInputConfigurationLoaded = null;

	public static DSpotCompiler getCompiler() {
		return compiler;
	}

	public static InputConfiguration getInputConfiguration() {
		return inputConfiguration;
	}

	public static void reset() {
		currentInputConfigurationLoaded = null;
		AutomaticBuilderFactory.reset();
		try {
			FileUtils.forceDelete(new File("tmpDir"));
		} catch (IOException ignored) {
			//ignored
		}
	}

	public static void init(String pathToConfFile) {
		if (pathToConfFile.equals(currentInputConfigurationLoaded)) {
			return;
		}
		try {
			AutomaticBuilderFactory.reset();
			currentInputConfigurationLoaded = pathToConfFile;
			inputConfiguration = new InputConfiguration(pathToConfFile);
			inputProgram = InitUtils.initInputProgram(inputConfiguration);
			InitUtils.initLogLevel(inputConfiguration);
			inputConfiguration.setInputProgram(inputProgram);
			File tmpDir = new File(inputConfiguration.getProperty("tmpDir"));
			if (!tmpDir.exists()) {
				tmpDir.mkdir();
			} else {
				try {
					FileUtils.cleanDirectory(tmpDir);
				} catch (Exception ignored) {
					// ignored
				}
			}
			FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(inputConfiguration.getProperty("tmpDir") + "/tmp"));
			final String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp/" +
					(inputConfiguration.getProperty("targetModule") == null ? "" : inputConfiguration.getProperty("targetModule"));
			inputProgram.setProgramDir(outputDirectory);
			AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);
			String dependencies = builder.buildClasspath(inputProgram.getProgramDir());
			File output = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
			File outputTest = new File(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());
			try {
				FileUtils.cleanDirectory(output);
				FileUtils.cleanDirectory(outputTest);
			} catch (IllegalArgumentException ignored) {
				//the target directory does not exist, do not need to clean it
			}

			DSpotCompiler.compile(inputProgram.getAbsoluteSourceCodeDir(), dependencies, output);
			DSpotCompiler.compile(inputProgram.getAbsoluteTestSourceCodeDir(),
					output.getAbsolutePath() + System.getProperty("path.separator") + dependencies, outputTest);
			compiler = new DSpotCompiler(inputProgram, dependencies);
			inputProgram.setFactory(compiler.getLauncher().getFactory());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static InputProgram getInputProgram() {
		return inputProgram;
	}

	public static CtClass findClass(String fullQualifiedName) {
		return getInputProgram().getFactory().Class().get(fullQualifiedName);
	}

	public static CtMethod findMethod(CtClass<?> ctClass, String methodName) {
		Set<CtMethod<?>> mths = ctClass.getMethods();
		return mths.stream()
				.filter(mth -> mth.getSimpleName().endsWith(methodName))
				.findFirst()
				.orElse(null);
	}

	public static CtMethod findMethod(String className, String methodName) {
		Set<CtMethod> mths = findClass(className).getMethods();
		return mths.stream()
				.filter(mth -> mth.getSimpleName().endsWith(methodName))
				.findFirst()
				.orElse(null);
	}

	public static Factory getFactory() {
		return getInputProgram().getFactory();
	}
}
