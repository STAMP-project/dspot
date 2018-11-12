package eu.stamp_project.test_framework;

import eu.stamp_project.test_framework.assertions.AssertEnum;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

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
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments) {
        throw new RuntimeException("Unsupported Operation");
    }

    @Override
    public CtMethod<?> prepareTestMethod(CtMethod<?> testMethod) {
        return testMethod;
    }
}
