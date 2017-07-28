package fr.inria.diversify.dspot.value;

import fr.inria.diversify.util.Log;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.List;
import java.util.stream.IntStream;

/**
 * User: Simon
 * Date: 07/01/16
 * Time: 10:40
 */
public class ValueCreator {

	private static final int MAX_ARRAY_SIZE = 5;
	public static int count = 0;

	public static CtLocalVariable createRandomLocalVar(CtTypeReference type) {
		return type.getFactory().createLocalVariable(type, "vc_" + count++, getRandomValue(type));
	}

	public static CtExpression<?> getRandomValue(CtTypeReference type) {
		if (AmplificationChecker.isPrimitive(type)) {
			return generatePrimitiveRandomValue(type);
		} else {
			try {
				if (type.getActualClass() == String.class) {
					return type.getFactory().createLiteral(AmplificationHelper.getRandomString(20));
				}
				// TODO add some generic type such as List, check DSpotMockedTest to have a use case
			} catch (SpoonClassNotFoundException exception) {
				// couldn't load the definition of the class, it may be a client class
				return createValue(type);
			}
		}
		Log.warn("Could not generate a random value");
		return null;
//		throw new RuntimeException();
	}

	public static CtLocalVariable createNull(CtTypeReference type) {
		Factory factory = type.getFactory();
		final CtLiteral<?> defaultExpression = factory.createLiteral(null);
		defaultExpression.addTypeCast(type);
		return factory.Code().createLocalVariable(type, "vc_" + count++, defaultExpression);
	}

	private static CtExpression createValue(CtTypeReference type) {
		if (AmplificationChecker.isArray(type)) {
			CtArrayTypeReference arrayType = (CtArrayTypeReference) type;
			CtTypeReference typeComponent = arrayType.getComponentType();
			final CtNewArray<?> newArray = type.getFactory().createNewArray();
			newArray.setType(typeComponent);
			IntStream.range(0, AmplificationHelper.getRandom().nextInt(MAX_ARRAY_SIZE))
					.mapToObj(i -> createValue(typeComponent))
					.forEach(newArray::addElement);
			return newArray;
		} else {
			return generateConstructionOf(type);
		}
	}

	private static CtExpression generateConstructionOf(CtTypeReference type) {
		CtConstructorCall<?> constructorCall = type.getFactory().createConstructorCall();
		constructorCall.setType(type);
		if (type.getDeclaration() != null) {
			final List<CtConstructor> constructors = type.getDeclaration().getElements(new TypeFilter<>(CtConstructor.class));
			if (!constructors.isEmpty()) {
				final CtConstructor<?> selectedConstructor = constructors.get(AmplificationHelper.getRandom().nextInt(constructors.size()));
				selectedConstructor.getParameters().forEach(parameter ->
						constructorCall.addArgument(getRandomValue(parameter.getType()))
				);
			}
		}
		return constructorCall;
	}

	private static CtExpression<?> generatePrimitiveRandomValue(CtTypeReference type) {
		if (type.getActualClass() == Boolean.class || type.getActualClass() == boolean.class) {
			return type.getFactory().createLiteral(AmplificationHelper.getRandom().nextBoolean());
		}
		if (type.getActualClass() == Character.class || type.getActualClass() == char.class) {
			return type.getFactory().createLiteral(AmplificationHelper.getRandomChar());
		}
		if (type.getActualClass() == Byte.class || type.getActualClass() == byte.class) {
			return type.getFactory().createLiteral((byte) AmplificationHelper.getRandom().nextInt());
		}
		if (type.getActualClass() == Short.class || type.getActualClass() == short.class) {
			return type.getFactory().createLiteral((short) AmplificationHelper.getRandom().nextInt());
		}
		if (type.getActualClass() == Integer.class || type.getActualClass() == int.class) {
			return type.getFactory().createLiteral((AmplificationHelper.getRandom().nextInt()));
		}
		if (type.getActualClass() == Long.class || type.getActualClass() == long.class) {
			return type.getFactory().createLiteral((long) AmplificationHelper.getRandom().nextInt());
		}
		if (type.getActualClass() == Float.class || type.getActualClass() == float.class) {
			return type.getFactory().createLiteral((float) AmplificationHelper.getRandom().nextDouble());
		}
		if (type.getActualClass() == Double.class || type.getActualClass() == double.class) {
			return type.getFactory().createLiteral(AmplificationHelper.getRandom().nextDouble());
		}
		throw new RuntimeException();
	}
}
