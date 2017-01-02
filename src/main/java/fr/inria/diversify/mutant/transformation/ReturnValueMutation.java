package fr.inria.diversify.mutant.transformation;

import spoon.reflect.code.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * User: Simon
 * Date: 17/02/14
 * Time: 17:29
 */
public class ReturnValueMutation extends MutationTransformation<CtReturn, CtStatement> {

    public  ReturnValueMutation(CtReturn transplantationPoint) {
        super(transplantationPoint);
        type = "mutation";
        name = "returnValue";
    }

    @Override
    public void buildTransplant()  {

        Factory factory = transplantationPoint.getFactory();
        if(isPrimitive(transplantationPoint.getReturnedExpression().getType())) {
            CtExpression newExpression = factory.Core().clone(transplantationPoint.getReturnedExpression());
            CtExpression mutant;

            String type = newExpression.getType().unbox().getSimpleName();
            if (type.equals("boolean")) {
                CtUnaryOperator unaryOperator = factory.Core().createUnaryOperator();
                unaryOperator.setKind(UnaryOperatorKind.NOT);
                unaryOperator.setOperand(newExpression);
                mutant = unaryOperator;
            } else {
                CtLiteral lit = factory.Code().createLiteral(1);
                mutant = factory.Code().createBinaryOperator(newExpression, lit, BinaryOperatorKind.PLUS);
            }
            transplant = transplantationPoint.getFactory().Core().clone(transplantationPoint);
            ((CtReturn)transplant).setReturnedExpression(mutant);
        } else {
            if(transplantationPoint.getReturnedExpression().toString().equals("null")) {
               CtThrow ctThrow = factory.Core().createThrow();
               ctThrow.setThrownExpression(factory.Code().createCodeSnippetExpression("new java.lang.RuntimeException()"));
                transplant = ctThrow;
            }  else {

            }
            transplant = transplantationPoint.getFactory().Core().clone(transplantationPoint);
            CtLiteral lit = factory.Core().createLiteral();
            lit.setType(factory.Type().nullType());
            ((CtReturn)transplant).setReturnedExpression(lit);
        }
    }

    protected boolean isPrimitive(CtTypeReference refType) {
        try {
            return refType.unbox().isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

}
