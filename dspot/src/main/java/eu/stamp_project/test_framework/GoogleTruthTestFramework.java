package eu.stamp_project.test_framework;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 */
public class GoogleTruthTestFramework extends AbstractTestFramework {

    public GoogleTruthTestFramework() {
        super("com.google.common.truth.Truth");
    }

    @Override
    public boolean isTest(CtMethod<?> candidate) throws UnsupportedTestFrameworkException {
        return false;
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, String methodName, List<CtExpression> arguments) {
        return null;
    }

}
