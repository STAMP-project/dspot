package eu.stamp_project;

import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.utils.options.InputConfiguration;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.List;
import java.util.Set;


/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:16
 */
public class Utils {

	public static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	private static DSpotCompiler compiler;

	public static DSpotCompiler getCompiler() {
		return compiler;
	}

	private static boolean hasACleanInstanceOfInputConfiguration = false;

	public static void reset() {
		InputConfiguration.reset();
		hasACleanInstanceOfInputConfiguration = true;
	}

	public static void init(String pathToConfFile) {
		try {
			FileUtils.forceDelete(new File(InputConfiguration.get().getOutputDirectory()));
		} catch (Exception ignored) {

		}
		try {
			FileUtils.forceDelete(new File("target/dspot/tmp_test_sources/"));
		} catch (Exception ignored) {

		}
		if (hasACleanInstanceOfInputConfiguration) {
			return;
		}
		try {
			InputConfiguration inputConfiguration = InputConfiguration.get();
			inputConfiguration.setVerbose(true);
			compiler = DSpotCompiler.createDSpotCompiler();
			inputConfiguration.setFactory(compiler.getLauncher().getFactory());
			inputConfiguration.setInputAmplDistributorEnum(InputAmplDistributorEnum.RandomInputAmplDistributor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static CtClass<?> findClass(String fullQualifiedName) {
		return InputConfiguration.get().getFactory().Class().get(fullQualifiedName);
	}

	public static CtType<?> findType(String fullQualifiedName) {
		return InputConfiguration.get().getFactory().Type().get(fullQualifiedName);
	}


	public static CtMethod<?> findMethod(CtClass<?> ctClass, String methodName) {
		Set<CtMethod<?>> mths = ctClass.getMethods();
		return mths.stream()
				.filter(mth -> mth.getSimpleName().endsWith(methodName))
				.findFirst()
				.orElse(null);
	}

	public static CtMethod findMethod(String className, String methodName) {
		return findClass(className).getMethods().stream()
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
				return TestFramework.get().isTest(element);
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
		return InputConfiguration.get().getFactory();
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
