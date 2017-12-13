package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/11/17
 */
@SuppressWarnings("unchecked")
public class AssertionRemover {

    private final int [] counter = new int[]{0};

    public CtMethod<?> removeAssertion(CtMethod<?> testMethod) {
        CtMethod<?> testWithoutAssertion = AmplificationHelper.cloneMethodTest(testMethod, "");
        testWithoutAssertion.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return AmplificationChecker.isAssert(element);
            }
        }).forEach(this::removeAssertion);
        return testWithoutAssertion;
    }

    public void removeAssertion(CtInvocation<?> invocation) {
        final Factory factory = invocation.getFactory();
        invocation.getArguments().forEach(argument -> {
            CtExpression clone = ((CtExpression) argument).clone();
            if (clone instanceof CtUnaryOperator) {
                clone = ((CtUnaryOperator) clone).getOperand();
            }
            if (clone instanceof CtStatement) {
                invocation.insertBefore((CtStatement) clone);
            } else if (! (clone instanceof CtLiteral || clone instanceof CtVariableRead)) {
                CtTypeReference<?> typeOfParameter = clone.getType();
                if (clone.getType().equals(factory.Type().NULL_TYPE)) {
                    typeOfParameter = factory.Type().createReference(Object.class);
                }
                final CtLocalVariable localVariable = factory.createLocalVariable(
                        typeOfParameter,
                        typeOfParameter.getSimpleName() + "_" + counter[0]++,
                        clone
                );
                invocation.insertBefore(localVariable);
            }
        });
        invocation.getParent(CtStatementList.class).removeStatement(invocation);
    }

}
