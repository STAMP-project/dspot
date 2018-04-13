package fr.inria.diversify.dspot.amplifier;

import spoon.reflect.code.CtLiteral;

import java.util.Collections;
import java.util.Set;

public class BooleanLiteralAmplifier extends AbstractLiteralAmplifier<Boolean> {
    @Override
    protected Set<Boolean> amplify(CtLiteral<Boolean> existingLiteral) {
        return Collections.singleton(!existingLiteral.getValue());
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
