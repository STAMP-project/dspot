package fr.inria.diversify.dspot.amplifier.value;

import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/10/17
 */
public class ConstructorCreator {

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
                                    clone.addArgument(ValueCreator.generateRandomValue(parameter.getType()))
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

    //TODO we should checks if at least the default constructor is available.
    //TODO we may need to implement a support for factory usages
    static CtExpression generateConstructionOf(CtTypeReference type) {
        CtType<?> typeDeclaration = type.getDeclaration() == null ? type.getTypeDeclaration() : type.getDeclaration();
        if (typeDeclaration != null) {
            final List<CtConstructor<?>> constructors = typeDeclaration.getElements(new TypeFilter<CtConstructor<?>>(CtConstructor.class) {
                @Override
                public boolean matches(CtConstructor<?> element) {
                    return element.hasModifier(ModifierKind.PUBLIC) &&
                            element.getParameters().stream()
                                    .map(CtParameter::getType)
                                    .allMatch(ValueCreatorHelper::canGenerateAValueForType);
                }
            });
            if (!constructors.isEmpty()) {
                CtConstructorCall<?> constructorCall = type.getFactory().createConstructorCall();
                constructorCall.setType(type);
                final CtConstructor<?> selectedConstructor = constructors.get(AmplificationHelper.getRandom().nextInt(constructors.size()));
                selectedConstructor.getParameters().forEach(parameter -> {
//                            if (!type.getActualTypeArguments().isEmpty()) {
//                                type.getActualTypeArguments().forEach(ctTypeReference -> {
//                                            if (!parameter.getType().getActualTypeArguments().contains(ctTypeReference)) {
//                                                parameter.getType().setActualTypeArguments(ctTypeReference);
//                                            }
//                                        }
//                                );
//                            }
                            constructorCall.addArgument(ValueCreator.generateRandomValue(parameter.getType()));
                        }
                );
                return constructorCall;
            }
        }
        return null;
    }

}
