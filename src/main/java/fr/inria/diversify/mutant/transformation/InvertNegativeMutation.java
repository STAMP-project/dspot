package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.CtLiteral;

/**
 * User: Simon
 * Date: 08/01/16
 * Time: 14:44
 */
public class InvertNegativeMutation extends MutationTransformation<CtLiteral, CtLiteral> {
    public InvertNegativeMutation(CtLiteral transplantationPoint) {
        super(transplantationPoint);
        type = "mutation";
        name = "invertNegative";
    }

    @Override
    protected void buildTransplant() {
        transplant = transplantationPoint.getFactory().Core().clone(transplantationPoint);
        Object value = transplant.getValue();
        Class literalType = transplant.getType().box().getActualClass();

        if(literalType.equals(Integer.class)) {
            transplant.setValue(-1 * (Integer)value);
        }
        if(literalType.equals(Long.class)) {
            transplant.setValue(-1 * (Long)value);
        }
        if(literalType.equals(Short.class)) {
            transplant.setValue(-1 * (Short)value);
        }
        if(literalType.equals(Double.class)) {
            transplant.setValue(-1 * (Double)value);
        }
        if(literalType.equals(Float.class)) {
            transplant.setValue(-1 * (Float)value);
        }
    }
}
