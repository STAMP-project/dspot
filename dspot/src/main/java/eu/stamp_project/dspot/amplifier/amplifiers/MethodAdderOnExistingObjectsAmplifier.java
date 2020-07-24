package eu.stamp_project.dspot.amplifier.amplifiers;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.MethodsHandler;
import eu.stamp_project.dspot.amplifier.amplifiers.value.ValueCreatorHelper;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
@SuppressWarnings("unchecked")
public class MethodAdderOnExistingObjectsAmplifier implements Amplifier {

    @Override
    public Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration) {
        List<CtLocalVariable<?>> existingObjects = getExistingObjects(testMethod);
        return existingObjects.stream()
                .flatMap(existingObject -> AmplifierHelper.findMethodsWithTargetType(existingObject.getType()).stream()
                        .filter(ctMethod ->
                                !MethodsHandler.isASupportedMethodName(ctMethod.getSimpleName())
                                        || (MethodsHandler.isASupportedMethodName(ctMethod.getSimpleName()) && !ctMethod.getParameters().isEmpty())
                        )
                        .filter(ctMethod -> ctMethod.getParameters()
                                .stream()
                                .map(CtParameter::getType)
                                .allMatch(ValueCreatorHelper::canGenerateAValueForType)
                        ).map(methodToBeAdd ->
                                AmplifierHelper.addInvocation(testMethod,
                                        methodToBeAdd,
                                        AmplifierHelper.createLocalVarRef(existingObject),
                                        existingObject,
                                        "_mg",
                                        "MethodAdderOnExistingObjectsAmplifier: added method on existing object")
                        ).map(amplifiedTestMethod -> {
                                    Counter.updateInputOf(amplifiedTestMethod, 1);
                                    return amplifiedTestMethod;
                                }
                        )
                        .collect(Collectors.toList()).stream()
                );
    }

    private List<CtLocalVariable<?>> getExistingObjects(CtMethod method) {
        return method.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class) {
            @Override
            public boolean matches(CtLocalVariable<?> element) {
                return element.getType() != null &&
                        !element.getType().isPrimitive() &&
                        element.getType().getDeclaration() != null;
            }
        });
    }


    @Override
    public void reset(CtType<?> testClass) {
        AmplificationHelper.reset();
    }
}
