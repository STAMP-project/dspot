package fr.inria;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.Initializer;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.apache.commons.io.FileUtils;
import org.pitest.reloc.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.List;
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
	}

	public static void init(String pathToConfFile) {
		try {
			FileUtils.forceDelete(new File(inputConfiguration.getOutputDirectory()));
		} catch (Exception ignored) {

		}
		try {
			FileUtils.forceDelete(new File("target/dspot/tmp_test_sources/"));
		} catch (Exception ignored) {

		}
		if (pathToConfFile.equals(currentInputConfigurationLoaded)) {
			return;
		}
		try {
			AmplificationHelper.minimize = false;
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

	public static List<CtMethod<?>> getAllTestMethodsFrom(String className) {
		return getAllTestMethodsFrom(getFactory().Type().get(className));
	}

	public static List<CtMethod<?>> getAllTestMethodsFrom(CtType<?> testClass) {
		return testClass.filterChildren(new TypeFilter<CtMethod<?>>(CtMethod.class) {
			@Override
			public boolean matches(CtMethod<?> element) {
				return AmplificationChecker.isTest(element);
			}
		}).list();
	}

	@SuppressWarnings("unchecked")
	public static <T> void replaceGivenLiteralByNewValue(CtQueryable parent, T newValue) {
		((CtLiteral<T>)parent.filterChildren(new FILTER_LITERAL_OF_GIVEN_TYPE(newValue.getClass()))
				.first())
				.replace(getFactory().createLiteral(newValue));
	}

	public static Factory getFactory() {
		return getInputProgram().getFactory();
	}

	public static final class FILTER_LITERAL_OF_GIVEN_TYPE extends TypeFilter<CtLiteral> {

		private Class<?> clazz;

		public FILTER_LITERAL_OF_GIVEN_TYPE(Class<?> clazz) {
			super(CtLiteral.class);
			this.clazz = clazz;
		}

		@Override
		public boolean matches(CtLiteral element) {
			return clazz.isAssignableFrom(element.getValue().getClass()) ||
					element.getValue().getClass().isAssignableFrom(clazz);
		}
	}
}
