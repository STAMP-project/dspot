package fr.inria.diversify.dspot.amplifier;

import edu.emory.mathcs.backport.java.util.Collections;
import spoon.reflect.code.CtLiteral;

import java.util.Set;

public class BooleanLiteralAmplifier extends AbstractLiteralAmplifier<Boolean> {
    @Override
    protected Set<CtLiteral<Boolean>> amplify(CtLiteral<Boolean> existingLiteral) {
        return Collections.singleton(existingLiteral.getFactory().createLiteral(!existingLiteral.getValue()));
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
