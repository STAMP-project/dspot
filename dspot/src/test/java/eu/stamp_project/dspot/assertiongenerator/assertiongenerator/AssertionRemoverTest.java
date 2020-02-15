package eu.stamp_project.dspot.assertiongenerator.assertiongenerator;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.TestWithLogGenerator;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.configuration.InitializeDSpot;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import org.junit.Test;
import spoon.Launcher;
import spoon.OutputType;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/06/17
 */
public class AssertionRemoverTest extends AbstractTestOnSample {

    @Test
    public void testRemoveAssertionsOnLambdaWithNullBody() {

        /*
            remove assertions that contains a lambda with a null body as follow:
                org.junit.jupiter.api.Assertions.assertTrue(((myApp.getMyAppSystemInformation(true)) != null), () -> "App should return some info")
         */

        CtClass testClass = findClass("fr.inria.sample.TestClassWithAssert");
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

        CtClass testClass = findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> test1 = (CtMethod<?>) testClass.getMethodsByName("testWithNewSomethingWithoutLocalVariables").get(0);
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(test1);
        final CtMethod<?> testWithLog =
                TestWithLogGenerator.createTestWithLog(ctMethod, "fr.inria.sample", Collections.emptyList());
        assertEquals("@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void testWithNewSomethingWithoutLocalVariables_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    java.lang.String o_testWithNewSomethingWithoutLocalVariables__1 = new fr.inria.sample.ClassWithBoolean().toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.ObjectLog.log(o_testWithNewSomethingWithoutLocalVariables__1, \"o_testWithNewSomethingWithoutLocalVariables__1\", \"testWithNewSomethingWithoutLocalVariables__1\");" + AmplificationHelper.LINE_SEPARATOR +
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
        final CtClass<?> testClass = findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> test = findMethod(testClass, "testWithAMethodCallThatContainsAssertionsAndItsReturnedValueIsUsed");
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
        final CtClass<?> testClass = findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithALambda = findMethod(testClass, "testWithTryWithResource");
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
        final CtClass<?> testClass = findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithALambda = findMethod(testClass, "testWithALambda");
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

        final CtClass<?> testClass = findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithCatchVariable = findMethod(testClass, "testWithArray");
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

        final CtClass<?> testClass = findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithCatchVariable = findMethod(testClass, "test3_exceptionCatch");
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
        final CtClass<?> testClass = findClass("fr.inria.sample.TestClassWithAssert");
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

        final CtClass<?> testClass = findClass("fr.inria.assertionremover.TestClassWithAssertToBeRemoved");
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

        final CtClass<?> testClass = findClass("fr.inria.assertionremover.TestClassWithAssertToBeRemoved");
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

        UserInput configuration = new UserInput();
        configuration.setAbsolutePathToProjectRoot(new File(getPathToProjectRoot()).getAbsolutePath());
//        new TestCompiler(0,
//                false,
//                configuration.getAbsolutePathToProjectRoot(),
//                configuration.getClasspathClassesProject(),
//                10000,
//                "",
//                false
//
//        );
        AutomaticBuilder builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        InitializeDSpot initializeDSpot = new InitializeDSpot();
        String dependencies = initializeDSpot.completeDependencies(configuration, builder);
        launcher = new Launcher();
        launcher.addInputResource(getPathToProjectRoot());
        launcher.getEnvironment().setOutputType(OutputType.CLASSES);
        launcher.getModelBuilder().setSourceClasspath(dependencies.split(AmplificationHelper.PATH_SEPARATOR));
        launcher.buildModel();

        final CtClass<?> testClass = findClass("fr.inria.helper.TestWithMultipleAsserts");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> testMethod = testClass.getMethodsByName("test").get(0);
        final CtMethod<?> removedAssertion = assertionRemover.removeAssertion(testMethod);
        assertEquals(4, removedAssertion.getBody().getStatements().size());
    }
}
