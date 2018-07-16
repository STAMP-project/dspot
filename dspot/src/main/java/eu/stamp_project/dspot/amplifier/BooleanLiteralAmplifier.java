package eu.stamp_project.dspot.amplifier;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;

import java.util.Collections;
import java.util.Set;

public class BooleanLiteralAmplifier extends AbstractLiteralAmplifier<Boolean> {

    @Override
    protected Set<CtLiteral<Boolean>> amplify(CtLiteral<Boolean> original, CtMethod<?> testMethod) {
        return Collections.singleton(testMethod.getFactory().createLiteral(!original.getValue()));
    }

    @Override
    protected String getSuffix() {
        return "litBool";
    }

    @Override
    protected Class<?> getTargetedClass() {
        return Boolean.class;
    }
}
