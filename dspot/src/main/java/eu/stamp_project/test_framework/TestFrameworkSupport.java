package eu.stamp_project.test_framework;

import eu.stamp_project.compare.ObjectLog;
import eu.stamp_project.test_framework.assertions.AssertEnum;
import eu.stamp_project.test_framework.assertions.IsAssertInvocationFilter;
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
    public boolean isTest(CtMethod<?> candidate);

    /**
     * Builds an invocation to <code>methodName</code>
     * @param testMethod test method under amplification
     * @param assertion the type of the assertion
     * @param arguments  the arguments of the assertion, <i>e.g.</i> the two element to be compared in {@link org.junit.Assert#assertEquals(Object, Object)}.
     *                   The order of these arguments is arbitrary.
     *                   Conventionally, the expected value is on the left of the assertion and the actual one is on the right.
     *                   So, it expects that the expected value is the first element, and the actual the second element. (When the assertion requires two values).
     * @return a spoon node representing the invocation to the assertion, ready to be inserted in a test method
     */
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments);

    /**
     *  key of metadata of spoon nodes to know if its result from amplification
     */
    public final static String METADATA_ASSERT_AMPLIFICATION = "A-Amplification";

    /**
     * prepare a test method for the amplification
     * @param testMethod the test method to be prepared
     * @return a prepared test method for the amplification
     */
    public CtMethod<?> prepareTestMethod(CtMethod<?> testMethod);

    /**
     * This method generate the code needed when a test set the program under a unstable state and throws an exception.
     * @param test the test method under amplification
     * @param failure the failure returned by the test runner
     * @param numberOfFail the number of amplified test methods that expect a exception to be thrown. This int is used to identify uniquely the test.
     * @return the test method expected the exception to be thrown by the program
     */
    public CtMethod<?> generateExpectedExceptionsBlock(CtMethod<?> test, Failure failure, int numberOfFail);

    /**
     * This method is responsible to add to the test class that will be run a method that will call {@link ObjectLog#save()}.
     * The test methods in the given test class should have calls to {@link ObjectLog#log(Object, String, String)}.
     * @param testClass the test class that contains test method with logs. This testClass reference will be directly modified.
     * @param testsToRun the list of instrumented test methods to be run
     */
    public void generateAfterClassToSaveObservations(CtType<?> testClass, List<CtMethod<?>> testsToRun);

}
