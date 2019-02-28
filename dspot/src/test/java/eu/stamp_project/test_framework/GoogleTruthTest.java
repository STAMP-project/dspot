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
import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/11/18
 */
public class GoogleTruthTest extends AbstractTest {

    @Test
    public void test() {

        /*
            Test the buildInvocation
         */

        // GOOGLE TRUTH
        final String fullQualifiedName = "fr.inria.helper.GoogleTruthTestClass";
        final String nameOfExpectedAssertClass = "com.google.common.truth.Truth.";
        final String test = "test";
        final CtClass<?> testClass = Utils.findClass(fullQualifiedName);
        final CtMethod testMethod = Utils.findMethod(fullQualifiedName, test);

        CtInvocation<?> ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_TRUE,
                Collections.singletonList(InputConfiguration.get().getFactory().createLiteral(true))
        );

        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(true).isTrue()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_FALSE,
                Collections.singletonList(InputConfiguration.get().getFactory().createLiteral(false))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(false).isFalse()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NULL,
                Collections.singletonList(InputConfiguration.get().getFactory().createLiteral(null))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(null).isNull()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NOT_NULL,
                Collections.singletonList(InputConfiguration.get().getFactory().createThisAccess(testClass.getReference()))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(this).isNotNull()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_EQUALS,
                Arrays.asList(
                        InputConfiguration.get().getFactory().createThisAccess(testClass.getReference()),
                        InputConfiguration.get().getFactory().createThisAccess(testClass.getReference())
                )
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(this).isEqualsTo(this)", ctInvocation.toString());

        TestFramework.get().generateAfterClassToSaveObservations(testClass, Collections.singletonList(testMethod));
        assertTrue(!testClass.getMethodsByName("afterClass").isEmpty());
    }
}
