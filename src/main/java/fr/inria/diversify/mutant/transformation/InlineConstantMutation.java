package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;

/**
 * User: Simon
 * Date: 08/01/16
 * Time: 15:53
 */
public class InlineConstantMutation extends MutationTransformation<CtLocalVariable, CtLocalVariable> {
    public InlineConstantMutation(CtLocalVariable transplantationPoint) {
        super(transplantationPoint);
        type = "mutation";
        name = "inlineConstant";
    }

    @Override
    protected void buildTransplant() {
        CtLiteral literal = (CtLiteral) transplantationPoint.getDefaultExpression();
        String type = literal.getType().getSimpleName();

        CtLiteral newLiteral = transplantationPoint.getFactory().Core().clone(literal);

        if(type.equals("boolean")) {
            newLiteral.setValue(!((Boolean)literal.getValue()));
        } else if(type.equals("short")) {
            newLiteral.setValue(((Short)literal.getValue() + 1));
        } else if(type.equals("int")) {
            newLiteral.setValue(((Integer)literal.getValue() + 1));
        } else if(type.equals("long")) {
            newLiteral.setValue(((Long)literal.getValue() + 1));
        } else if(type.equals("byte")) {
            newLiteral.setValue(((Byte)literal.getValue() + 1));
        } else if(type.equals("float")) {
            newLiteral.setValue(((Float)literal.getValue() + 1.0f));
        } else if(type.equals("double")) {
            newLiteral.setValue(((Double)literal.getValue() + 1.0d));
        }

        transplant = transplantationPoint.getFactory().Core().clone(transplantationPoint);
        transplant.setDefaultExpression(newLiteral);
    }
}
