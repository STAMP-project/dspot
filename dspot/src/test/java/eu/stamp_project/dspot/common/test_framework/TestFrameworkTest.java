package eu.stamp_project.dspot.common.test_framework;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.test_framework.assertions.AssertEnum;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 */
public class TestFrameworkTest extends AbstractTestOnSample {

    @Before
    @Override
    public void setUp() {
        super.setUp();
        UserInput configuration = new UserInput();
        configuration.setAbsolutePathToProjectRoot(new File("src/test/resources/sample/").getAbsolutePath());
        DSpotUtils.init(CommentEnum.Amplifier, "target/dspot/", configuration
                .getFullClassPathWithExtraDependencies(), "src/test/resources/sample/");
    }

    private CtMethod findAndRegister(String ctClass, String methodName) {
        final CtMethod testExpectingAnException = findMethod(ctClass, methodName);
        AmplificationHelper.addTestBindingToOriginal(testExpectingAnException, testExpectingAnException);
        return testExpectingAnException;
    }

    @Test
    public void testJUnit3TestFrameworkSupportsTwoAssertClasses() {
        /*
            The test framework for junit 3 should recognized both class as assert class:
                - junit.framework.TestCase
                - junit.framework.Assert
         */

        CtMethod testJUnit3 = this.findMethod("fr.inria.helper.SecondClassJUnit3", "testUsingDeprecatedAssertClass");
        System.out.println(testJUnit3);
        assertTrue(TestFramework.get().isTest(testJUnit3));
        assertTrue(TestFramework.get().isAssert(testJUnit3.getBody().getStatement(0)));
        testJUnit3 = this.findMethod("fr.inria.helper.SecondClassJUnit3", "test");
        System.out.println(testJUnit3);
        assertTrue(TestFramework.get().isTest(testJUnit3));
        assertTrue(TestFramework.get().isAssert(testJUnit3.getBody().getStatement(0)));
    }

    @Test
    public void testGenerateAfterClassToSaveObservations() {

        /*
            Test generate after class to save observations
         */

        final CtMethod<?> testJUnit3 = this.findAndRegister("fr.inria.helper.SecondClassJUnit3", "testExpectingAnException");
        final CtType type = this.launcher.getFactory().Type().get("fr.inria.helper.SecondClassJUnit3");
        TestFramework.get().generateAfterClassToSaveObservations(type, Collections.singletonList(testJUnit3));
        final String expectedToString = "public static junit.framework.Test suite() {" + AmplificationHelper.LINE_SEPARATOR +
//                "    return new junit.extensions.TestSetup(new junit.framework.TestSuite(fr.inria.helper.SecondClassJUnit3.class)) {" + AmplificationHelper.LINE_SEPARATOR +
                "    return new junit.extensions.TestSetup(fr.inria.helper.SecondClassJUnit3.class) {" + AmplificationHelper.LINE_SEPARATOR +
                "        protected void tearDown() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "            eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.ObjectLog.save();" + AmplificationHelper.LINE_SEPARATOR +
                "        }" + AmplificationHelper.LINE_SEPARATOR +
                "    };" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedToString, type.getMethodsByName("suite").get(0).toString());
    }

    @Test
    public void testGenerateExpectedExceptionsBlock() {

        /*
            Test the generation of code that make a test expecting a given Exception
         */
        final CtMethod testJUnit3 = this.findAndRegister("fr.inria.helper.SecondClassJUnit3", "testExpectingAnException");
        final CtMethod<?> actualJUnit3 = TestFramework.get().generateExpectedExceptionsBlock(
                testJUnit3,
                new Failure("", "", new RuntimeException()),
                0
        );
        assertEquals(actualJUnit3.toString(), JUnit3WithExceptionThrown, actualJUnit3.toString());

        final CtMethod testJUnit4 = this.findAndRegister("fr.inria.helper.TestWithMultipleAsserts", "testThrownException");
        final CtMethod<?> actualJUnit4 = TestFramework.get().generateExpectedExceptionsBlock(
                testJUnit4,
                new Failure("", "", new RuntimeException()),
                0
        );
        assertEquals(actualJUnit4.toString(), JUnit4WithExceptionThrown, actualJUnit4.toString());

        final CtMethod testJUnit5 = this.findAndRegister("fr.inria.testframework.TestSupportJUnit5", "testExpectAnException");
        final CtMethod<?> actualJUnit5 = TestFramework.get().generateExpectedExceptionsBlock(
                testJUnit5,
                new Failure("", "", new RuntimeException()),
                0
        );
        assertEquals(actualJUnit5.toString(), JUnit5WithExceptingThrown, actualJUnit5.toString());
    }

    private final static String JUnit3WithExceptionThrown = "public void testExpectingAnException_failAssert0() {" + AmplificationHelper.LINE_SEPARATOR +
            "    // AssertionGenerator generate try/catch block with fail statement" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        junit.framework.TestCase.assertTrue(true);" + AmplificationHelper.LINE_SEPARATOR +
            "        throw new java.lang.RuntimeException();" + AmplificationHelper.LINE_SEPARATOR +
            "        junit.framework.TestCase.fail(\"testExpectingAnException should have thrown RuntimeException\");" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (java.lang.RuntimeException expected) {" + AmplificationHelper.LINE_SEPARATOR +
            "        junit.framework.TestCase.assertEquals(null, expected.getMessage());" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}";

    private final static String JUnit4WithExceptionThrown = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
            "public void testThrownException_failAssert0() {" + AmplificationHelper.LINE_SEPARATOR +
            "    // AssertionGenerator generate try/catch block with fail statement" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        throw new java.lang.RuntimeException();" + AmplificationHelper.LINE_SEPARATOR +
            "        org.junit.Assert.fail(\"testThrownException should have thrown RuntimeException\");" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (java.lang.RuntimeException expected) {" + AmplificationHelper.LINE_SEPARATOR +
            "        org.junit.Assert.assertEquals(null, expected.getMessage());" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}";

    private final static String JUnit5WithExceptingThrown = "@org.junit.jupiter.api.Test" + AmplificationHelper.LINE_SEPARATOR +
            "public void testExpectAnException_failAssert0() {" + AmplificationHelper.LINE_SEPARATOR +
            "    // AssertionGenerator generate try/catch block with fail statement" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.jupiter.api.Assertions.assertThrows(java.lang.RuntimeException.class, () -> {" + AmplificationHelper.LINE_SEPARATOR +
            "        org.junit.jupiter.api.Assertions.assertTrue(true);" + AmplificationHelper.LINE_SEPARATOR +
            "        throwAnException();" + AmplificationHelper.LINE_SEPARATOR +
            "    });" + AmplificationHelper.LINE_SEPARATOR +
            "}";

    @Test
    public void testIsTest() {
        /*
            Test that we can different unit test:
                JUnit3
                JUnit4
                JUnit5
         */

        //JUnit3
        final CtMethod testJUnit3 = this.findAndRegister("fr.inria.helper.SecondClassJUnit3", "test");
        assertTrue(TestFramework.get().isTest(testJUnit3));
        final CtMethod testJUnit3WithInheritance = this.findAndRegister("fr.inria.helper.ThirdClassJUnit3", "test");
        assertTrue(TestFramework.get().isTest(testJUnit3WithInheritance));
        //JUnit4
        final CtMethod testJUnit4 = this.findAndRegister("fr.inria.helper.TestWithMultipleAsserts", "test");
        assertTrue(TestFramework.get().isTest(testJUnit4));
        //JUnit5
        final CtMethod testJUnit5 = this.findAndRegister("fr.inria.helper.ClassWithInnerClass", "Junit5Test");
        assertTrue(TestFramework.get().isTest(testJUnit5));

        // NOT A TEST
        CtMethod currentNotATest = this.findAndRegister("fr.inria.helper.ClassWithInnerClass", "notATestBecauseEmpty");
        assertFalse(TestFramework.get().isTest(currentNotATest));
        currentNotATest = this.findAndRegister("fr.inria.helper.ClassWithInnerClass", "notATestBecauseParameters");
        assertFalse(TestFramework.get().isTest(currentNotATest));
        currentNotATest = this.findAndRegister("fr.inria.helper.ClassWithInnerClass", "methodIntermediate1");
        assertFalse(TestFramework.get().isTest(currentNotATest));
    }

    @Test
    public void testBuildInvocationJUnit() {

        /*
            Test the generation of invocation to specific assert methods
         */

        // JUNIT 3
        checksBuildInvocationForGivenJUnitVersion(
                "fr.inria.helper.SecondClassJUnit3",
                "test",
                "junit.framework.TestCase."
        );

        // JUNIT 4
        checksBuildInvocationForGivenJUnitVersion(
                "fr.inria.helper.ClassWithInnerClass",
                "test",
                "org.junit.Assert."
        );

        // JUNIT 5
        checksBuildInvocationForGivenJUnitVersion(
                "fr.inria.helper.ClassWithInnerClass",
                "Junit5Test",
                "org.junit.jupiter.api.Assertions."
        );
    }

    private void checksBuildInvocationForGivenJUnitVersion(String fullQualifiedName, String test, String nameOfExpectedAssertClass) {
        final CtClass<?> testClass = findClass(fullQualifiedName);
        final CtMethod testMethod = this.findAndRegister(fullQualifiedName, test);
        CtInvocation<?> ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_TRUE,
                Collections.singletonList(this.launcher.getFactory().createLiteral(true))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertTrue(true)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_FALSE,
                Collections.singletonList(this.launcher.getFactory().createLiteral(false))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertFalse(false)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NULL,
                Collections.singletonList(this.launcher.getFactory().createLiteral(null))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertNull(null)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NOT_NULL,
                Collections.singletonList(this.launcher.getFactory().createThisAccess(testClass.getReference()))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertNotNull(this)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_EQUALS,
                Arrays.asList(
                        this.launcher.getFactory().createThisAccess(testClass.getReference()),
                        this.launcher.getFactory().createThisAccess(testClass.getReference())
                )
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertEquals(this, this)", ctInvocation.toString());
    }
}
