package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.assertgenerator.components.AssertionRemover;
import eu.stamp_project.dspot.assertgenerator.components.utils.AssertionGeneratorUtils;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/06/17
 */
public class AssertionRemoverTest extends AbstractTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        Utils.reset();
    }

    @Test
    public void testRemoveAssertionsOnLambdaWithNullBody() {

        /*
            remove assertions that contains a lambda with a null body as follow:
                org.junit.jupiter.api.Assertions.assertTrue(((myApp.getMyAppSystemInformation(true)) != null), () -> "App should return some info")
         */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> test1 = (CtMethod<?>) testClass.getMethodsByName("testWithALambdaWithNullBody").get(0);
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(test1);
        final String expected = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
                "public void testWithALambdaWithNullBody() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    new fr.inria.sample.ClassThrowException().throwException();" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expected, ctMethod.toString());
    }

    @Test
    public void testThatWeGenerateLogStatementOnValuesThatWasAssertedByTheOriginalTest() {

        /*
            We execute the assertion remover then the instrumentation of the logging
            w/e was in the assertion. DSpot generates a log statement around it
                e.g. assertEquals("aString", new MyObject().toString()) would give
                new MyObject().toString(); then
                log(new MyObject().toString())
         */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> test1 = (CtMethod<?>) testClass.getMethodsByName("testWithNewSomethingWithoutLocalVariables").get(0);
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(test1);
        final CtMethod<?> testWithLog =
                AssertionGeneratorUtils.createTestWithLog(ctMethod, "fr.inria.sample", Collections.emptyList());
        assertEquals("@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void testWithNewSomethingWithoutLocalVariables_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    java.lang.String o_testWithNewSomethingWithoutLocalVariables__1 = new fr.inria.sample.ClassWithBoolean().toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(o_testWithNewSomethingWithoutLocalVariables__1, \"o_testWithNewSomethingWithoutLocalVariables__1\", \"testWithNewSomethingWithoutLocalVariables__1\");" + AmplificationHelper.LINE_SEPARATOR +
                "}", testWithLog.toString());
    }

    @Test
    public void testRemoveInvocationWhenReturnedValueIsUsed() {

        /*
            Test that when a method call that contains assertions AND
                its returned type is used in the test is not removed
                TODO this may produce some failing tests
                TODO however, we consider developers that makes such invocations
                TODO should be aware that the oracles must not rely on state of the current test
         */
        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> test = Utils.findMethod(testClass, "testWithAMethodCallThatContainsAssertionsAndItsReturnedValueIsUsed");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(test);
        final String expectedMethodString = "@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void testWithAMethodCallThatContainsAssertionsAndItsReturnedValueIsUsed() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    java.lang.String aString = verify(\"aString\");" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethodString, ctMethod.toString());
    }

    @Test
    public void testOnTestWithTryWithResource() {
        /*
            Test that the AssertionRemoverTest is able to remove assertion but not try with resources
         */
        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithALambda = Utils.findMethod(testClass, "testWithTryWithResource");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(testWithALambda);
        final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    try (final java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File(\".\"))) {" + AmplificationHelper.LINE_SEPARATOR +
                "        fis.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedBody, ctMethod.getBody().toString());
    }

    @Test
    public void testOnAssertionWithALambda() {
        /*
            Test that we can remove the assertion on a lambda expression
         */
        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithALambda = Utils.findMethod(testClass, "testWithALambda");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(testWithALambda);
        System.out.println(ctMethod);
        assertFalse(ctMethod.getBody().getStatements().isEmpty());
    }

    @Test
    public void testOnTestMethodWithNonJavaIdentifier() throws Exception {

        /*
            test that we can remove assert that have type that are not correct java identifier
         */

        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithCatchVariable = Utils.findMethod(testClass, "testWithArray");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(testWithCatchVariable);
        assertTrue(
                ctMethod.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class))
                        .stream()
                        .map(CtLocalVariable::getSimpleName)
                        .allMatch(string -> {
                                    try {
                                        for (int i = 0; i < string.length(); i++) {
                                            if (!Character.isJavaIdentifierPart(string.charAt(i))) {
                                                return false;
                                            }
                                        }
                                        return true;
                                    } catch (Exception ignored) {
                                        return false;
                                    }
                                }
                        )
        );

    }

    @Test
    public void testOnCatchVariable() throws Exception {

        /*
            We remove try/catch block and Assert.fail() statement if any.
         */

        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithCatchVariable = Utils.findMethod(testClass, "test3_exceptionCatch");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(testWithCatchVariable);
        assertTrue(ctMethod.getElements(new TypeFilter<>(CtCatch.class)).isEmpty());
        assertTrue(ctMethod.getElements(new TypeFilter<>(CtTry.class)).isEmpty());
        assertTrue(ctMethod.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return element.getExecutable().getSimpleName().equals("fail");
            }
        }).isEmpty());
    }

    @Test
    public void testRemoveAssertionOnSimpleExample() throws Exception {
        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final AssertionRemover assertionRemover = new AssertionRemover();
        testClass.getMethodsByName("test1").get(0).getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return TestFramework.get().isAssert(element);
            }
        }).forEach(assertionRemover::removeAssertion);

        final String expectedMethod = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
                "public void test1() {" + AmplificationHelper.LINE_SEPARATOR +
                "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
                "    cl.getTrue();" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethod, testClass.getMethodsByName("test1").get(0).toString());
    }

    @Test
    public void testRemoveAssertionWithSwitchCase() throws Exception {

		/*
            Test that the AssertionRemover remove assertions from tests when the assertion is inside a case:
		 */

        final CtClass<?> testClass = Utils.findClass("fr.inria.assertionremover.TestClassWithAssertToBeRemoved");
        final AssertionRemover assertionRemover = new AssertionRemover();
        testClass.getMethodsByName("test1").get(0).getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return TestFramework.get().isAssert(element);
            }
        }).forEach(assertionRemover::removeAssertion);

        final String expectedMethod = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
                "public void test1() {" + AmplificationHelper.LINE_SEPARATOR +
                "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
                "    cl.getTrue();" + AmplificationHelper.LINE_SEPARATOR +
                "    int one = 1;" + AmplificationHelper.LINE_SEPARATOR +
                "    switch (one) {" + AmplificationHelper.LINE_SEPARATOR +
                "        case 1 :" + AmplificationHelper.LINE_SEPARATOR +
                "            fr.inria.assertionremover.TestClassWithAssertToBeRemoved.getNegation(cl.getFalse());" + AmplificationHelper.LINE_SEPARATOR +
                "            break;" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethod, testClass.getMethodsByName("test1").get(0).toString());
    }

    @Test
    public void testRemoveAssertionWithUnary() throws Exception {

		/*
            Test that the AssertionRemover remove assertions from tests when assertions used unary operators
		 */

        final CtClass<?> testClass = Utils.findClass("fr.inria.assertionremover.TestClassWithAssertToBeRemoved");
        final AssertionRemover assertionRemover = new AssertionRemover();
        testClass.getMethodsByName("test2").get(0).getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return TestFramework.get().isAssert(element);
            }
        }).forEach(assertionRemover::removeAssertion);

        final String expectedMethod = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
                "public void test2() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    int a = -1;" + AmplificationHelper.LINE_SEPARATOR +
                "    int b = 1;" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethod, testClass.getMethodsByName("test2").get(0).toString());
    }

    @Test
    public void testOnDifferentKindOfAssertions() throws Exception {
        /*
            Test that the AssertionRemover remove all kind of assertions
		 */

        final CtClass<?> testClass = Utils.findClass("fr.inria.helper.TestWithMultipleAsserts");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> testMethod = testClass.getMethodsByName("test").get(0);
        final CtMethod<?> removedAssertion = assertionRemover.removeAssertion(testMethod);
        assertEquals(4, removedAssertion.getBody().getStatements().size());
    }
}
