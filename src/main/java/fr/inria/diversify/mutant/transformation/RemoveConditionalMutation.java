package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.Factory;

/**
 * User: Simon
 * Date: 13/02/14
 * Time: 14:45
 */
public class RemoveConditionalMutation extends MutationTransformation<CtExpression, CtLiteral<Boolean>> {

    public RemoveConditionalMutation(CtExpression transplantationPoint) {
        super(transplantationPoint);
        type = "mutation";
        name = "removeConditional";
    }


    @Override
    protected void buildTransplant() {
        Factory factory = transplantationPoint.getFactory();
        transplant = factory.Core().createLiteral();
        transplant.setValue(true);
    }
}
