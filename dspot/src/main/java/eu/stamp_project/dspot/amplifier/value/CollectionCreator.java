package eu.stamp_project.dspot.amplifier.value;

import eu.stamp_project.utils.RandomHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/10/17
 */
public class CollectionCreator {

    static CtExpression<?> generateCollection(CtTypeReference type, String nameMethod, Class<?> typeOfCollection) {
        if (type.getActualTypeArguments().stream().anyMatch(reference -> reference instanceof CtWildcardReference)
                || RandomHelper.getRandom().nextBoolean()) {
            return generateEmptyCollection(type, "empty" + nameMethod, typeOfCollection);
        } else {
            return generateSingletonList(type,
                    "singleton" + ("Set".equals(nameMethod) ? "" : nameMethod),
                    typeOfCollection
            );
        }
    }

    @SuppressWarnings("unchecked")
    static CtExpression<?> generateSingletonList(CtTypeReference type, String nameMethod, Class<?> typeOfCollection) {
        final Factory factory = type.getFactory();
        final CtType<?> collectionsType = factory.Type().get(Collections.class);
        final CtTypeAccess<?> accessToCollections = factory.createTypeAccess(collectionsType.getReference());
        final CtMethod<?> singletonListMethod = collectionsType.getMethodsByName(nameMethod).get(0);
        final CtExecutableReference executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName(singletonListMethod.getSimpleName());
        executableReference.setDeclaringType(collectionsType.getReference());
        executableReference.setType(factory.createCtTypeReference(typeOfCollection));
        if (!type.getActualTypeArguments().isEmpty() &&
                type.getActualTypeArguments().stream().allMatch(ValueCreatorHelper::canGenerateAValueForType)) {
            executableReference.setParameters(type.getActualTypeArguments());
            List<CtExpression<?>> parameters = type.getActualTypeArguments().stream()
                    .map(reference -> ValueCreator.generateRandomValue(reference, 0)).collect(Collectors.toList());
            return factory.createInvocation(accessToCollections, executableReference, parameters);
        } else {
            return factory.createInvocation(accessToCollections, executableReference,
                    factory.createConstructorCall(factory.Type().createReference(Object.class))
            );
        }
    }

    static CtExpression<?> generateEmptyCollection(CtTypeReference type, String nameMethod, Class<?> typeOfCollection) {
        final Factory factory = type.getFactory();
        final CtType<?> collectionsType = factory.Type().get(Collections.class);
        final CtTypeAccess<?> accessToCollections = factory.createTypeAccess(collectionsType.getReference());
        final CtMethod<?> singletonListMethod = collectionsType.getMethodsByName(nameMethod).get(0);
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName(singletonListMethod.getSimpleName());
        executableReference.setDeclaringType(collectionsType.getReference());
        executableReference.setType(factory.createCtTypeReference(typeOfCollection));
        if (type.getActualTypeArguments().isEmpty()) {
//          supporting Collections.<type>emptyList()
            executableReference.addActualTypeArgument(type);
        } else if (type.getActualTypeArguments()
                .stream()
                .noneMatch(reference -> reference instanceof CtWildcardReference)){// in case type is a list, we copy the Actual arguments
            executableReference.setActualTypeArguments(type.getActualTypeArguments());
        }
        return factory.createInvocation(accessToCollections, executableReference);
    }

}
