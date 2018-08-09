package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.dspot.amplifier.value.ValueCreatorHelper;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.CloneHelper;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.stream.Stream;

public class ReplacementAmplifier implements Amplifier {

    @SuppressWarnings("unchecked")
    @Override
    public Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration) {
        return testMethod.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class) {
            @Override
            public boolean matches(CtLocalVariable element) {
                return !element.getSimpleName().contains("DSPOT") &&
                        element.getParent() instanceof CtBlock &&
                        ValueCreatorHelper.canGenerateAValueForType(element.getType());
            }
        }).stream()
                .map(ctLocalVariable -> {
                    final CtMethod clone = CloneHelper.cloneTestMethodForAmp(testMethod, "_replacement");
                    final CtLocalVariable localVariable = clone.getElements(new TypeFilter<>(CtLocalVariable.class))
                            .stream()
                            .filter(ctLocalVariable1 -> ctLocalVariable1.equals(ctLocalVariable))
                            .findFirst()
                            .get();
                    CtExpression<?> ctExpression = ValueCreator.generateRandomValue(ctLocalVariable.getType(), 0, localVariable.getAssignment());
                    localVariable.setAssignment(ctExpression);
                    return clone;
                });
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
    }
}
