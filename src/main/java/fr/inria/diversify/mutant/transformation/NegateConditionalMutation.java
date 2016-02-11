package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.factory.Factory;

/**
 * User: Simon
 * Date: 13/02/14
 * Time: 14:44
 */
public class NegateConditionalMutation extends MutationTransformation<CtBinaryOperator, CtBinaryOperator> {

    public NegateConditionalMutation(CtBinaryOperator transplantationPoint) {
        super(transplantationPoint);
        type = "mutation";
        name = "negateConditional";
    }

    @Override
    protected void buildTransplant() {
        Factory factory = transplantationPoint.getFactory();
        transplant = factory.Core().clone(transplantationPoint);


        BinaryOperatorKind kind = transplantationPoint.getKind();
        if(kind.equals(BinaryOperatorKind.EQ))
            transplant.setKind(BinaryOperatorKind.NE);
        if(kind.equals(BinaryOperatorKind.NE))
            transplant.setKind(BinaryOperatorKind.EQ);

        if(kind.equals(BinaryOperatorKind.LE))
            transplant.setKind(BinaryOperatorKind.GT);
        if(kind.equals(BinaryOperatorKind.GE))
            transplant.setKind(BinaryOperatorKind.LT);

        if(kind.equals(BinaryOperatorKind.LT))
            transplant.setKind(BinaryOperatorKind.GE);
        if(kind.equals(BinaryOperatorKind.GT))
            transplant.setKind(BinaryOperatorKind.LE);
    }
}
