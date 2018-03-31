package fr.inria.diversify.dspot.amplifier.value;

import fr.inria.diversify.utils.AmplificationChecker;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/10/17
 */
public class ValueCreatorHelper {

    public static boolean canGenerateAValueForType(CtTypeReference type) {
        try {
            if (AmplificationChecker.isPrimitive(type)) {
                return true;
            } else {
                try {
                    if (AmplificationChecker.isArray(type) ||
                            type.getActualClass() == String.class ||
                            type.getActualClass() == Collection.class ||
                            type.getActualClass() == List.class ||
                            type.getActualClass() == Set.class ||
                            type.getActualClass() == Map.class) {
                        return true;
                    }
                } catch (SpoonClassNotFoundException exception) {
                    // couldn't load the definition of the class, it may be a client class
                    return canGenerateConstructionOf(type);
                }
            }
            return canGenerateConstructionOf(type);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean canGenerateConstructionOf(CtTypeReference type) {
        CtType<?> typeDeclaration = type.getDeclaration() == null ? type.getTypeDeclaration() : type.getDeclaration();

        if (typeDeclaration == null) {
            return false;
        }

        final boolean canBeConstructed = (!typeDeclaration.getElements(new TypeFilter<CtConstructor<?>>(CtConstructor.class) {
            // we can use at least one constructor
            @Override
            public boolean matches(CtConstructor<?> element) {
                return element.hasModifier(ModifierKind.PUBLIC) &&
                        element.getParameters()
                                .stream()
                                .map(CtParameter::getType)
                                .filter(reference -> ! reference.equals(type))
                                .allMatch(ValueCreatorHelper::canGenerateAValueForType);
            }
        }).isEmpty()) ||
                // or we can use a factor method
                !(type.getFactory().getModel().getElements(new ConstructorCreator.FILTER_FACTORY_METHOD(type)).isEmpty());
        // above, when we say use, it means that we can find one element that match filters, i.e. the returned list is not empty

        return (type.getActualTypeArguments().isEmpty() || // There is no type arguments
                        !type.getActualTypeArguments().isEmpty() && // or there is, and
                                type.getActualTypeArguments().stream().noneMatch( // none is Wildcard, e.g. <E extends Object>
                                        reference -> reference instanceof CtWildcardReference
                                )
                ) && !type.getModifiers().contains(ModifierKind.ABSTRACT) && canBeConstructed;
    }
}
