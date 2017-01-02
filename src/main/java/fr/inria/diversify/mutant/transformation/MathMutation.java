package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.factory.Factory;

/**
 * User: Simon
 * Date: 13/02/14
 * Time: 14:46
 */
public class MathMutation extends MutationTransformation<CtBinaryOperator, CtBinaryOperator> {

    public MathMutation(CtBinaryOperator transplantationPoint) {
        super(transplantationPoint);
        name = "math";
        type = "mutation";
    }

    @Override
    protected void buildTransplant() {
        Factory factory = transplantationPoint.getFactory();
        transplant = factory.Core().clone(transplantationPoint);

        BinaryOperatorKind kind = transplantationPoint.getKind();
        if(kind.equals(BinaryOperatorKind.PLUS))
            transplant.setKind(BinaryOperatorKind.MINUS);
        if(kind.equals(BinaryOperatorKind.MINUS))
            transplant.setKind(BinaryOperatorKind.PLUS);

        if(kind.equals(BinaryOperatorKind.MUL))
            transplant.setKind(BinaryOperatorKind.DIV);
        if(kind.equals(BinaryOperatorKind.DIV))
            transplant.setKind(BinaryOperatorKind.MUL);

        if(kind.equals(BinaryOperatorKind.MOD))
            transplant.setKind(BinaryOperatorKind.MUL);

        if(kind.equals(BinaryOperatorKind.BITAND))
            transplant.setKind(BinaryOperatorKind.BITOR);
        if(kind.equals(BinaryOperatorKind.BITOR))
            transplant.setKind(BinaryOperatorKind.BITAND);

        if(kind.equals(BinaryOperatorKind.SL))
            transplant.setKind(BinaryOperatorKind.SR);
        if(kind.equals(BinaryOperatorKind.SR))
            transplant.setKind(BinaryOperatorKind.SL);

        if(kind.equals(BinaryOperatorKind.USR))
            transplant.setKind(BinaryOperatorKind.SL);
    }
}
