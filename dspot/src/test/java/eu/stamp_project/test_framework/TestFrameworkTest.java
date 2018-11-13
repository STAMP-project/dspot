package eu.stamp_project.test_framework;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.test_framework.assertions.AssertEnum;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 */
public class TestFrameworkTest extends AbstractTest {

    @Test
    public void testIsTest() {
        /*
            Test that we can different unit test:
                JUnit3
                JUnit4
                JUnit5
         */

        //JUnit3
        final CtMethod testJUnit3 = Utils.findMethod("fr.inria.helper.SecondClassJUnit3", "test");
        assertTrue(TestFramework.get().isTest(testJUnit3));
        //JUnit4
        final CtMethod testJUnit4 = Utils.findMethod("fr.inria.helper.TestWithMultipleAsserts", "test");
        assertTrue(TestFramework.get().isTest(testJUnit4));
        //JUnit5
        final CtMethod testJUnit5 = Utils.findMethod("fr.inria.helper.ClassWithInnerClass", "Junit5Test");
        assertTrue(TestFramework.get().isTest(testJUnit5));

        // NOT A TEST
        CtMethod currentNotATest = Utils.findMethod("fr.inria.helper.ClassWithInnerClass", "notATestBecauseEmpty");
        assertFalse(TestFramework.get().isTest(currentNotATest));
        currentNotATest = Utils.findMethod("fr.inria.helper.ClassWithInnerClass", "notATestBecauseParameters");
        assertFalse(TestFramework.get().isTest(currentNotATest));
        currentNotATest = Utils.findMethod("fr.inria.helper.ClassWithInnerClass", "methodIntermediate1");
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
        final CtClass<?> testClass = Utils.findClass(fullQualifiedName);
        final CtMethod testMethod = Utils.findMethod(fullQualifiedName, test);
        CtInvocation<?> ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_TRUE,
                Collections.singletonList(InputConfiguration.get().getFactory().createLiteral(true))
        );

        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertTrue(true)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_FALSE,
                Collections.singletonList(InputConfiguration.get().getFactory().createLiteral(false))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertFalse(false)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NULL,
                Collections.singletonList(InputConfiguration.get().getFactory().createLiteral(null))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertNull(null)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NOT_NULL,
                Collections.singletonList(InputConfiguration.get().getFactory().createThisAccess(testClass.getReference()))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertNotNull(this)", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_EQUALS,
                Arrays.asList(
                        InputConfiguration.get().getFactory().createThisAccess(testClass.getReference()),
                        InputConfiguration.get().getFactory().createThisAccess(testClass.getReference())
                )
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertEquals(this, this)", ctInvocation.toString());
    }
}
