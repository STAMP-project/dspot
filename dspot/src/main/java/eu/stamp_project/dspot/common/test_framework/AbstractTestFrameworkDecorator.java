package eu.stamp_project.dspot.common.test_framework;

import eu.stamp_project.dspot.common.test_framework.assertions.AssertEnum;
import eu.stamp_project.dspot.common.test_framework.implementations.NoneTestFrameworkSupport;
import eu.stamp_project.testrunner.runner.Failure;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/02/19
 *
 * Decorator design pattern around TestFrameworkSupport
 *
 */
public abstract class AbstractTestFrameworkDecorator extends AbstractTestFramework {

    private TestFrameworkSupport innerTestFramework;

    public AbstractTestFrameworkDecorator(String qualifiedNameOfAssertClass) {
        super(qualifiedNameOfAssertClass);
        this.innerTestFramework = new NoneTestFrameworkSupport();
    }

    public AbstractTestFrameworkDecorator(String qualifiedNameOfAssertClass, TestFrameworkSupport innerTestFramework) {
        super(qualifiedNameOfAssertClass);
        this.innerTestFramework = innerTestFramework;
    }

    public void setInnerTestFramework(TestFrameworkSupport innerTestFramework) {
        this.innerTestFramework = innerTestFramework;
    }

    @Override
    public boolean isTest(CtMethod<?> candidate) {
        return this.innerTestFramework.isTest(candidate);
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments) {
        return this.innerTestFramework.buildInvocationToAssertion(testMethod, assertion, arguments);
    }

    @Override
    public CtMethod<?> prepareTestMethod(CtMethod<?> testMethod) {
        return this.innerTestFramework.prepareTestMethod(testMethod);
    }

    @Override
    public CtMethod<?> generateExpectedExceptionsBlock(CtMethod<?> test, Failure failure, int numberOfFail) {
        return this.innerTestFramework.generateExpectedExceptionsBlock(test, failure, numberOfFail);
    }

    @Override
    public void generateAfterClassToSaveObservations(CtType<?> testClass, List<CtMethod<?>> testsToRun) {
        this.innerTestFramework.generateAfterClassToSaveObservations(testClass,testsToRun);
    }
}
