package fr.inria.diversify.dspot.amplifier.value;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
		return ValueCreator.createRandomLocalVar(type, "vc");
	}

	public static CtLocalVariable createRandomLocalVar(CtTypeReference type, String prefixName) {
		final CtExpression<?> randomValue = generateRandomValue(type);
		if (randomValue == null) {
			return null;
		}
		return type.getFactory().createLocalVariable(type, prefixName + "_" + count++, randomValue);
	}

	private static CtExpression<?> generateRandomValue(CtTypeReference type) {
		if (AmplificationChecker.isPrimitive(type)) {
			return generatePrimitiveRandomValue(type);
		} else {
			try {
				if (AmplificationChecker.isArray(type)) {
					return generateArray(type);
				} else if (type.getActualClass() == String.class) {
					return type.getFactory().createLiteral(AmplificationHelper.getRandomString(20));
				} else if (type.getActualClass() == Collection.class || type.getActualClass() == List.class) {
					return generateCollection(type, "List", List.class);
				} else if (type.getActualClass() == Set.class) {
					return generateCollection(type, "Set", Set.class);
				} else if (type.getActualClass() == Map.class) {
					return generateCollection(type, "Map", Map.class);
				}
			} catch (SpoonClassNotFoundException exception) {
				// couldn't load the definition of the class, it may be a client class
				return generateConstructionOf(type);
			}
		}
		return generateConstructionOf(type);
//		throw new RuntimeException();
	}

	private static CtExpression<?> generateCollection(CtTypeReference type, String nameMethod, Class<?> typeOfCollection) {
		if (AmplificationHelper.getRandom().nextBoolean()) {
			return generateEmptyCollection(type, "empty"+nameMethod, typeOfCollection);
		} else {
			return generateSingletonList(type,
					"singleton"+ ("Set".equals(nameMethod) ? "" : nameMethod),
					typeOfCollection
			);
		}
	}

	@SuppressWarnings("unchecked")
	private static CtExpression<?> generateSingletonList(CtTypeReference type, String nameMethod, Class<?> typeOfCollection) {
		final Factory factory = type.getFactory();
		final CtType<?> collectionsType = factory.Type().get(Collections.class);
		final CtTypeAccess<?> accessToCollections = factory.createTypeAccess(collectionsType.getReference());
		final CtMethod<?> singletonListMethod = collectionsType.getMethodsByName(nameMethod).get(0);
		final CtExecutableReference executableReference = factory.Core().createExecutableReference();
		executableReference.setStatic(true);
		executableReference.setSimpleName(singletonListMethod.getSimpleName());
		executableReference.setDeclaringType(collectionsType.getReference());
		executableReference.setType(factory.createCtTypeReference(typeOfCollection));
		if (!type.getActualTypeArguments().isEmpty()) {
			executableReference.setParameters(type.getActualTypeArguments());
			List<CtExpression<?>> parameters = type.getActualTypeArguments().stream()
					.map(ValueCreator::generateRandomValue).collect(Collectors.toList());
			return factory.createInvocation(accessToCollections, executableReference,parameters);
		} else {
			return factory.createInvocation(accessToCollections, executableReference,
					factory.createConstructorCall(factory.Type().createReference(Object.class))
			);
		}
	}

	private static CtExpression<?> generateEmptyCollection(CtTypeReference type, String nameMethod, Class<?> typeOfCollection) {
		final Factory factory = type.getFactory();
		final CtType<?> collectionsType = factory.Type().get(Collections.class);
		final CtTypeAccess<?> accessToCollections = factory.createTypeAccess(collectionsType.getReference());
		final CtMethod<?> singletonListMethod = collectionsType.getMethodsByName(nameMethod).get(0);
		final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
		executableReference.setStatic(true);
		executableReference.setSimpleName(singletonListMethod.getSimpleName());
		executableReference.setDeclaringType(collectionsType.getReference());
		executableReference.setType(factory.createCtTypeReference(typeOfCollection));
		return factory.createInvocation(accessToCollections, executableReference);
	}

	public static CtLocalVariable generateNullValue(CtTypeReference type) {
		Factory factory = type.getFactory();
		final CtLiteral<?> defaultExpression = factory.createLiteral(null);
		defaultExpression.addTypeCast(type);
		return factory.Code().createLocalVariable(type, "vc_" + count++, defaultExpression);
	}

	private static CtExpression generateArray(CtTypeReference type) {
		CtArrayTypeReference arrayType = (CtArrayTypeReference) type;
		CtTypeReference typeComponent = arrayType.getComponentType();
		CtNewArray<?> newArray = type.getFactory().createNewArray();
		final int size = AmplificationHelper.getRandom().nextInt(MAX_ARRAY_SIZE);
		newArray.setType(arrayType);
		if (size == 0) {
			newArray.setDimensionExpressions(Collections.singletonList(type.getFactory().createLiteral(size)));
		} else {
			IntStream.range(0, size)
					.mapToObj(i -> generateRandomValue(typeComponent))
					.forEach(newArray::addElement);
		}
		return newArray;
	}

	public static List<CtExpression> generateAllConstructionOf(CtTypeReference type) {
		CtConstructorCall<?> constructorCall = type.getFactory().createConstructorCall();
		constructorCall.setType(type);
		if (type.getDeclaration() != null) {
			final List<CtConstructor<?>> constructors = type.getDeclaration().getElements(new TypeFilter<>(CtConstructor.class));
			if (!constructors.isEmpty()) {
				final List<CtExpression> generatedConstructors = constructors.stream().map(ctConstructor -> {
							final CtConstructorCall<?> clone = constructorCall.clone();
							ctConstructor.getParameters().forEach(parameter ->
									clone.addArgument(generateRandomValue(parameter.getType()))
							);
							return clone;
						}
				).collect(Collectors.toList());
				//add a null value
				final CtExpression<?> literalNull = type.getFactory().createLiteral(null);
				literalNull.setType(type);
				generatedConstructors.add(literalNull);
				return generatedConstructors;
			}
		}
		return Collections.singletonList(constructorCall);
	}

	private static CtExpression generateConstructionOf(CtTypeReference type) {
		CtConstructorCall<?> constructorCall = type.getFactory().createConstructorCall();
		constructorCall.setType(type);
		CtType<?> typeDeclaration = type.getDeclaration() == null ? type.getTypeDeclaration() : type.getDeclaration();
		if (typeDeclaration  != null) {
			final List<CtConstructor> constructors = typeDeclaration.getElements(new TypeFilter<CtConstructor>(CtConstructor.class) {
				@Override
				public boolean matches(CtConstructor element) {
					return element.hasModifier(ModifierKind.PUBLIC);
				}
			});
			if (!constructors.isEmpty()) {
				final CtConstructor<?> selectedConstructor = constructors.get(AmplificationHelper.getRandom().nextInt(constructors.size()));
				selectedConstructor.getParameters().forEach(parameter ->
						constructorCall.addArgument(generateRandomValue(parameter.getType()))
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
