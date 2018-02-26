package fr.inria.diversify;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.Initializer;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.util.Set;

import static fr.inria.diversify.utils.AmplificationHelper.PATH_SEPARATOR;


/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:16
 */
public class Utils {

	public static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	private static AutomaticBuilder builder;

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

	public static AutomaticBuilder getBuilder() {
		return builder;
	}

	public static void reset() {
		currentInputConfigurationLoaded = null;
		AutomaticBuilderFactory.reset();
	}

	public static void init(String pathToConfFile) {
		if (pathToConfFile.equals(currentInputConfigurationLoaded)) {
			return;
		}
		try {
			AutomaticBuilderFactory.reset();
			if (! new File("target/dspot/dependencies/compare").exists()) {
				DSpotUtils.copyPackageFromResources("fr/inria/diversify/compare/",
						"MethodsHandler", "ObjectLog", "Observation", "Utils");
			}
			currentInputConfigurationLoaded = pathToConfFile;
			inputConfiguration = new InputConfiguration(pathToConfFile);
			Initializer.initialize(inputConfiguration);
			inputProgram = inputConfiguration.getInputProgram();
			builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);
			String dependencies = builder.buildClasspath(inputProgram.getProgramDir());
			if (inputConfiguration.getProperty("additionalClasspathElements") != null) {
				dependencies += PATH_SEPARATOR + inputConfiguration.getInputProgram().getProgramDir()
						+ inputConfiguration.getProperty("additionalClasspathElements");
			}
			compiler = DSpotCompiler.createDSpotCompiler(inputProgram, dependencies);
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
