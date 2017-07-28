package fr.inria.diversify.dspot.value;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.SpoonClassNotFoundException;

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
		Factory factory = type.getFactory();
		CtExpression value = createValue(type);
		if (value != null) {
			return factory.Code().createLocalVariable(type, "vc_" + count++, value);
		} else {
			return null;
		}
	}

	public static CtLocalVariable createNull(CtTypeReference type) {
		Factory factory = type.getFactory();
		final CtLiteral<?> defaultExpression = factory.createLiteral(null);
		defaultExpression.addTypeCast(type);
		return factory.Code().createLocalVariable(type, "vc_" + count++, defaultExpression);
	}

	private static CtExpression createValue(CtTypeReference type) {
		Factory factory = type.getFactory();
		if (AmplificationChecker.isPrimitive(type)) {
			return generatePrimitiveRandomValue(type);
		} else if (AmplificationChecker.isArray(type)) {
			CtArrayTypeReference arrayType = (CtArrayTypeReference) type;
			CtTypeReference typeComponent = arrayType.getComponentType();
			final CtNewArray<?> newArray = factory.createNewArray();
			newArray.setType(typeComponent);
			IntStream.range(0, AmplificationHelper.getRandom().nextInt(MAX_ARRAY_SIZE))
					.mapToObj(i -> createValue(typeComponent))
					.forEach(newArray::addElement);
			return newArray;
		} else {
			CtConstructorCall<?> constructorCall = factory.createConstructorCall();
			constructorCall.setType(type);
			return constructorCall;
		}
	}

	public static CtExpression<?> getRandomValue(CtTypeReference type) {
		if (AmplificationChecker.isPrimitive(type)) {
			return generatePrimitiveRandomValue(type);
		} else {
			try {
				if (type.getActualClass() == String.class) {
					return type.getFactory().createLiteral(AmplificationHelper.getRandomString(20));
				}
			} catch (SpoonClassNotFoundException exception) {
				// could load the definition of the class, it may be a client class
				return createValue(type);
			}
		}
		throw new RuntimeException();
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
