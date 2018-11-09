package eu.stamp_project.test_framework;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 */
public class AssertJTestFramework extends AbstractTestFramework {

    public AssertJTestFramework() {
        super("org.assertj.core.api.*");
    }

    @Override
    public boolean isTest(CtMethod<?> candidate) throws UnsupportedTestFrameworkException {
        return false;
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion() {
        return null;
    }
}
