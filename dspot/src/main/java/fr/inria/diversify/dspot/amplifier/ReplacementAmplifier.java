package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.dspot.amplifier.value.ValueCreatorHelper;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;

public class ReplacementAmplifier implements Amplifier {

    @Override
    public List<CtMethod> apply(CtMethod testMethod) {
        return testMethod.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class) {
            @Override
            public boolean matches(CtLocalVariable element) {
                return !element.getSimpleName().contains("DSPOT") &&
                        element.getParent() instanceof CtBlock &&
                        ValueCreatorHelper.canGenerateAValueForType(element.getType());
            }
        }).stream()
                .map(ctLocalVariable -> {
                    final CtMethod clone = AmplificationHelper.cloneTestMethodForAmp(testMethod, "_replacement");
                    final CtLocalVariable localVariable = clone.getElements(new TypeFilter<>(CtLocalVariable.class))
                            .stream()
                            .filter(ctLocalVariable1 -> ctLocalVariable1.equals(ctLocalVariable))
                            .findFirst()
                            .get();
                    CtExpression<?> ctExpression = ValueCreator.generateRandomValue(ctLocalVariable.getType(), localVariable.getAssignment());
                    localVariable.setAssignment(ctExpression);
                    return clone;
                }).collect(Collectors.toList());
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
    }
}
