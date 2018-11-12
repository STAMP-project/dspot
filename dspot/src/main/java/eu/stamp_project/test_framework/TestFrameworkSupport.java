package eu.stamp_project.test_framework;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 *
 * Implementation of this interface should be stateless.
 *
 * If you implement a new TestFrameworkSupport, you should update the list of frameworks of the {@link TestFramework}
 *
 */
public interface TestFrameworkSupport {

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
     * Check that the given element in inside an assertion
     * @param candidate the element
     * @return true if a parent is an invocation to an assertion
     */
    public boolean isInAssert(CtElement candidate);

    /**
     * @param candidate the potential test method
     * @return true of the given candidate is a test method, false otherwise
     */
    public boolean isTest(CtMethod<?> candidate) throws UnsupportedTestFrameworkException;

    /**
     * Builds an invocation to <code>methodName</code>
     * @param testMethod test method under amplification
     * @param assertion the type of the assertion
     * @param arguments  the arguments of the assertion, <i>e.g.</i> the two element to be compared in {@link org.junit.Assert#assertEquals(Object, Object)}
     * @return a spoon node representing the invocation to the assertion, ready to be inserted in a test method
     */
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments);

    /**
     *  key of metadata of spoon nodes to know if its result from amplification
     */
    public final static String METADATA_ASSERT_AMPLIFICATION = "A-Amplification";

}
