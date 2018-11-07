package eu.stamp_project.test_framework;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public interface TestFrameworkSupport {

    /**
     * This methods tells if the given test class is using the supported test framework
     * @param testClass the test class
     * @return true if the implementation of {@link TestFrameworkSupport} support the used test framework, false otherwise
     */
    public boolean isMyTestFramework(CtClass<?> testClass);

    /**
     * @param invocation check if this CtInvocation is directly an assertion call or an invocation to a method that contains assertion calls.
     * @return true if the candidate is an assertion or call a method that contains assertions.
     */
    public boolean isAssert(CtInvocation<?> invocation);

    /**
     * This method is a proxy for {@link IsAssertInvocationFilter#isAssert(CtInvocation)} for CtStatement.
     * @param candidate check if this CtStatement is directly an assertion call or an invocation to a method that contains assertion calls.
     * @return true if the candidate is an assertion or call a method that contains assertions.
     */
    public boolean isAssert(CtStatement candidate);

    /**
     * @param candidate the potential test method
     * @return true of the given candidate is a test method, false otherwise
     */
    public boolean isTest(CtMethod<?> candidate);

    /**
     * @return an invocation to the corresponding assertion, with the correct arguments.
     */
    public CtInvocation<?> buildInvocationToAssertion();

}
