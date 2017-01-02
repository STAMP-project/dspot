package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.factory.Factory;

/**
 * User: Simon
 * Date: 17/02/14
 * Time: 17:30
 */
public class IncrementMutation extends MutationTransformation<CtUnaryOperator, CtUnaryOperator> {

    public IncrementMutation(CtUnaryOperator transplantationPoint) {
        super(transplantationPoint);
        type = "mutation";
        name = "increment";
    }

    @Override
    protected void buildTransplant() {
        Factory factory = transplantationPoint.getFactory();
        transplant = factory.Core().clone(transplantationPoint);

        UnaryOperatorKind kind = transplantationPoint.getKind();

        if(kind.equals(UnaryOperatorKind.PREINC)) {
            transplant.setKind(UnaryOperatorKind.PREDEC);
        }
        if(kind.equals(UnaryOperatorKind.PREDEC)) {
            transplant.setKind(UnaryOperatorKind.PREINC);
        }
        if(kind.equals(UnaryOperatorKind.POSTINC)) {
            transplant.setKind(UnaryOperatorKind.POSTDEC);
        }
        if(kind.equals(UnaryOperatorKind.POSTDEC)) {
            transplant.setKind(UnaryOperatorKind.POSTINC);
        }
    }
}
