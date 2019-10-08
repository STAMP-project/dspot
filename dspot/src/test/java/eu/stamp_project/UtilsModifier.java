package eu.stamp_project;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.utils.program.InputConfiguration;
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
public class UtilsModifier {

	@SuppressWarnings("unchecked")
	public static <T> void replaceGivenLiteralByNewValue(Factory factory, CtQueryable parent, T newValue) {
		((CtLiteral<T>)parent.filterChildren(new FILTER_LITERAL_OF_GIVEN_TYPE(newValue.getClass()))
				.first())
				.replace(factory.createLiteral(newValue));
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
