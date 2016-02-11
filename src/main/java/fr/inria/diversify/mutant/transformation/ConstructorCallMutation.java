package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.Factory;

/**
 * User: Simon
 * Date: 08/01/16
 * Time: 15:54
 */
public class ConstructorCallMutation extends MutationTransformation<CtConstructorCall, CtExpression> {

    public ConstructorCallMutation(CtConstructorCall transplantationPoint) {
        super(transplantationPoint);
        type = "mutation";
        name = "constructorCall";
    }

    @Override
    protected void buildTransplant() {
        Factory factory = transplantationPoint.getFactory();
        transplant = factory.Core().createLiteral();
        transplant.setType(factory.Type().nullType());
    }
}
