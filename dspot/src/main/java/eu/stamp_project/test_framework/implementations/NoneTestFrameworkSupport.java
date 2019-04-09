package eu.stamp_project.test_framework.implementations;

import eu.stamp_project.test_framework.TestFrameworkSupport;
import eu.stamp_project.test_framework.assertions.AssertEnum;
import eu.stamp_project.testrunner.runner.Failure;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/02/19
 */
public class NoneTestFrameworkSupport implements TestFrameworkSupport {

    @Override
    public boolean isAssert(CtInvocation<?> invocation) {
        return false;
    }

    @Override
    public boolean isAssert(CtStatement candidate) {
        return false;
    }

    @Override
    public boolean isInAssert(CtElement candidate) {
        return false;
    }

    @Override
    public boolean isTest(CtMethod<?> candidate) {
        return false;
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments) {
        return null;
    }

    @Override
    public CtMethod<?> prepareTestMethod(CtMethod<?> testMethod) {
        return null;
    }

    @Override
    public CtMethod<?> generateExpectedExceptionsBlock(CtMethod<?> test, Failure failure, int numberOfFail) {
        return null;
    }

    @Override
    public void generateAfterClassToSaveObservations(CtType<?> testClass, List<CtMethod<?>> testsToRun) {

    }
}
