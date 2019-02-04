package eu.stamp_project.prettifier;

import eu.stamp_project.test_framework.TestFramework;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/02/19
 */
public class RedundantCastRemover {


    public CtMethod<?> remove(CtMethod<?> testMethod) {
        final List<CtInvocation<?>> assertions = testMethod.getElements(TestFramework.ASSERTIONS_FILTER);
        for (CtInvocation<?> assertion : assertions) {
            this.remove(assertion);
        }
        return testMethod;
    }

    private void remove(CtInvocation<?> assertion) {
        final CtExpression<?> actualValue = assertion.getArguments().get(assertion.getArguments().size() - 1);
        final CtExpression<?> expectedValue = assertion.getArguments().get(assertion.getArguments().size() - 2);
        // top cast compared to the expected value
        if (!actualValue.getTypeCasts().isEmpty() &&
                actualValue.getTypeCasts().get(0).equals(expectedValue.getType())) {
            actualValue.getTypeCasts().remove(0);
        }
        // inner casts that can be removed
        removeCastInvocations(actualValue);
    }

    private void removeCastInvocations(CtExpression<?> current) {
        while (current instanceof CtInvocation<?>) {
            current = ((CtInvocation) current).getTarget();
            if (!current.getTypeCasts().isEmpty() &&
                    matchTypes(current.getTypeCasts().get(0), current.getType())) {
                current.getTypeCasts().remove(0);
            }
        }
    }

    private boolean matchTypes(CtTypeReference<?> toBeMatched, CtTypeReference<?> type) {
        if (type ==  null) {
            return false;
        } else if (toBeMatched.equals(type)) {
            return true;
        } else {
            return matchTypes(toBeMatched, type.getSuperclass());
        }
    }

}
