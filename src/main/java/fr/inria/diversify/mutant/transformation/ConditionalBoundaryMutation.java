package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.factory.Factory;


/**
 * User: Simon
 * Date: 11/02/14
 * Time: 11:47
 */
public class ConditionalBoundaryMutation extends MutationTransformation<CtBinaryOperator, CtBinaryOperator> {

    public ConditionalBoundaryMutation(CtBinaryOperator transplantationPoint) {
        super(transplantationPoint);
        name = "conditionalBoundary";
        type = "mutation";
    }

    protected void buildTransplant() {
        Factory factory = transplantationPoint.getFactory();
        transplant = factory.Core().clone(transplantationPoint);

        transplant.setParent(transplantationPoint.getParent());

        BinaryOperatorKind kind = transplantationPoint.getKind();
        if(kind.equals(BinaryOperatorKind.LT))
            transplant.setKind(BinaryOperatorKind.LE);
        if(kind.equals(BinaryOperatorKind.LE))
            transplant.setKind(BinaryOperatorKind.LT);
        if(kind.equals(BinaryOperatorKind.GT))
            transplant.setKind(BinaryOperatorKind.GE);
        if(kind.equals(BinaryOperatorKind.GE))
            transplant.setKind(BinaryOperatorKind.GT);
    }


}
