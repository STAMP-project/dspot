package eu.stamp_project.dspot.amplifier.value;

import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.RandomHelper;
import spoon.SpoonException;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/10/17
 */
public class ConstructorCreator {

    @Deprecated
    public static List<CtExpression> generateAllConstructionOf(CtTypeReference type) {
        CtConstructorCall<?> constructorCall = type.getFactory().createConstructorCall();
        constructorCall.setType(type);
        if (type.getDeclaration() != null) {
            final List<CtConstructor<?>> constructors = type.getDeclaration()
                    .getElements(new TypeFilter<CtConstructor<?>>(CtConstructor.class) {
                        @Override
                        public boolean matches(CtConstructor<?> element) {
                            return element.getParameters().stream()
                                    .map(CtParameter::getType)
                                    .allMatch(ValueCreatorHelper::canGenerateAValueForType);
                        }
                    });
            if (!constructors.isEmpty()) {
                final List<CtExpression> generatedConstructors = constructors.stream().map(ctConstructor -> {
                            final CtConstructorCall<?> clone = constructorCall.clone();
                            ctConstructor.getParameters().forEach(parameter ->
                                    clone.addArgument(ValueCreator.generateRandomValue(parameter.getType(), 0))
                            );
                            return clone;
                        }
                ).collect(Collectors.toList());
                //add a null value
                final CtExpression<?> literalNull = type.getFactory().createLiteral(null);
                literalNull.setType(type);
                //add all constructor via factory
                generatedConstructors.add(literalNull);
                return generatedConstructors;
            }
        }
        return Collections.singletonList(constructorCall);
    }

    private static class ComparatorPerCostToGenerateParameters implements Comparator<CtConstructor<?>> {
        @Override
        public int compare(CtConstructor<?> constructor1, CtConstructor<?> constructor2) {
            return this.costToGenerateParameters(constructor1.getParameters()) - this.costToGenerateParameters(constructor2.getParameters());
        }

        private int costToGenerateParameters(List<CtParameter<?>> parameters) {
            return parameters.stream().mapToInt(this::costOfOneParameter).sum();
        }

        private int costOfOneParameter(CtParameter<?> parameter) {
            final CtTypeReference<?> type = parameter.getType();
            if (AmplificationChecker.isPrimitive(type)) {
                return 0;
            } else {
                try {
                    if (AmplificationChecker.isArray(type)) {
                        return 1;
                        // now it may throw a SpoonClassNotFoundException, if it is a client class
                    } else if (type.getActualClass() == String.class) {
                        return 0;
                    } else if (type.getActualClass() == Collection.class ||
                            type.getActualClass() == List.class) {
                        return 3;
                    } else if (type.getActualClass() == Set.class) {
                        return 3;
                    } else if (type.getActualClass() == Map.class) {
                        return 3;
                    }
                } catch (SpoonException exception) {
                    // couldn't load the definition of the class, it may be a client class
                    return 4;
                }
            }
            return 4;
        }
    }

    //TODO we should checks if at least the default constructor is available.
    //TODO we may need to implement a support for factory usages
    static CtExpression generateConstructionOf(CtTypeReference type, int depth, CtExpression<?>... expressionsToAvoid) {
        CtType<?> typeDeclaration = type.getDeclaration() == null ? type.getTypeDeclaration() : type.getDeclaration();
        if (typeDeclaration != null && typeDeclaration instanceof CtClass<?>) {
            // We take public constructor that have only parameter that can be generated
            final List<CtConstructor<?>> constructors = ((CtClass<?>) typeDeclaration).getConstructors()
                    .stream()
                    .filter(ctConstructor ->
                            ctConstructor.hasModifier(ModifierKind.PUBLIC) &&
                                    ctConstructor.getParameters().stream()
                                            .map(CtParameter::getType)
                                            .allMatch(ValueCreatorHelper::canGenerateAValueForType)
                    ).collect(Collectors.toList());
            if (!constructors.isEmpty()) {
                CtConstructorCall<?> constructorCall = type.getFactory().createConstructorCall();
                constructorCall.setType(type);
                final CtConstructor<?> selectedConstructor;
                if (depth > 3) {
                    Collections.sort(constructors, new ComparatorPerCostToGenerateParameters());
                    selectedConstructor = constructors.get(0);
                } else {
                    selectedConstructor = constructors.get(RandomHelper.getRandom().nextInt(constructors.size()));
                }
                selectedConstructor.getParameters().forEach(parameter ->
                        constructorCall.addArgument(ValueCreator.generateRandomValue(parameter.getType(), depth + 1))
                );
                return constructorCall;
            } else {
                return buildConstructorWithFactory(type, expressionsToAvoid);
            }
        } else {
            return buildConstructorWithFactory(type, expressionsToAvoid);
        }
    }

    private static CtExpression buildConstructorWithFactory(CtTypeReference type, CtExpression<?>[] expressionsToAvoid) {
        final List<CtExpression<?>> constructorWithFactoryMethod = generateConstructorUsingFactory(type);
        if (!constructorWithFactoryMethod.isEmpty()) {
            CtExpression<?> selectedConstructor = constructorWithFactoryMethod
                    .remove(RandomHelper.getRandom().nextInt(constructorWithFactoryMethod.size()));
            while (!constructorWithFactoryMethod.isEmpty() &&
                    Arrays.stream(expressionsToAvoid).anyMatch(selectedConstructor::equals)) {
                selectedConstructor = constructorWithFactoryMethod
                        .remove(RandomHelper.getRandom().nextInt(constructorWithFactoryMethod.size()));
            }
            final CtTypeReference<?> declaringType = ((CtInvocation<?>) selectedConstructor).getExecutable().getDeclaringType();
            ((CtInvocation<?>) selectedConstructor).setTarget(selectedConstructor.getFactory().createTypeAccess(declaringType));
            return selectedConstructor;
        }
        return type.getFactory().createLiteral(null);
    }

    // we may need to be more exhaustive in the name convention of factories
    private static final String[] NAME_OF_FACTORY_METHOD = {"build", "create"};

    static final class FILTER_FACTORY_METHOD extends TypeFilter<CtMethod<?>> {
        private final CtTypeReference type;

        public FILTER_FACTORY_METHOD(CtTypeReference type) {
            super(CtMethod.class);
            this.type = type;
        }

        @Override
        public boolean matches(CtMethod<?> element) {
            return element.getModifiers().contains(ModifierKind.STATIC) &&
                    Arrays.stream(NAME_OF_FACTORY_METHOD)
                            .anyMatch(element.getSimpleName().toLowerCase()::contains) &&
                    element.getType().equals(type) &&
                    element.getParameters().stream()
                            .map(CtParameter::getType)
                            .allMatch(ValueCreatorHelper::canGenerateAValueForType);
        }
    }

    static List<CtExpression<?>> generateConstructorUsingFactory(final CtTypeReference type) {
        // this method will return an invocation of method that return the given type.
        // the usage of Factory classes/methods is well spread
        final Factory factory = type.getFactory();
        final CtTypeReference<?> referenceToBeBuild;
        if (type.getDeclaration() instanceof CtInterface<?>) { // we will find a sub class to build
            final Optional<CtClass<?>> first = factory.getModel().getElements(new TypeFilter<CtClass<?>>(CtClass.class) {
                @Override
                public boolean matches(CtClass<?> element) {
                    return element.getSuperInterfaces().contains(type.getDeclaration());
                }
            }).stream().findFirst();
            if (!first.isPresent()) {
                return Collections.emptyList();
            } else {
                referenceToBeBuild = first.get().getReference();
            }
        } else {
            referenceToBeBuild = type;
        }
        final List<CtMethod<?>> factoryMethod = factory.getModel().getElements(new FILTER_FACTORY_METHOD(referenceToBeBuild));
        return factoryMethod.stream()
                .map(method ->
                        factory.createInvocation(factory.createTypeAccess(method.getParent(CtType.class).getReference(), true),
                                method.getReference(),
                                method.getParameters().stream()
                                        .map(parameter -> ValueCreator.generateRandomValue(parameter.getType(), 0))
                                        .collect(Collectors.toList())
                        )
                ).collect(Collectors.toList());
    }

}
