package eu.stamp_project.dspot.common.test_framework;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.common.test_framework.assertions.AssertEnum;
import org.junit.Ignore;
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
public class GoogleTruthTest extends AbstractTestOnSample {

    @Ignore
    @Test
    public void test() {

        /*
            Test the buildInvocation
         */

        // GOOGLE TRUTH
        final String fullQualifiedName = "fr.inria.helper.GoogleTruthTestClass";
        final String nameOfExpectedAssertClass = "com.google.common.truth.Truth.";
        final String test = "test";
        final CtClass<?> testClass = findClass(fullQualifiedName);
        final CtMethod testMethod = findMethod(fullQualifiedName, test);

        CtInvocation<?> ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_TRUE,
                Collections.singletonList(this.launcher.getFactory().createLiteral(true))
        );

        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(true).isTrue()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_FALSE,
                Collections.singletonList(this.launcher.getFactory().createLiteral(false))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(false).isFalse()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NULL,
                Collections.singletonList(this.launcher.getFactory().createLiteral(null))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(null).isNull()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_NOT_NULL,
                Collections.singletonList(this.launcher.getFactory().createThisAccess(testClass.getReference()))
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(this).isNotNull()", ctInvocation.toString());

        ctInvocation = TestFramework.get().buildInvocationToAssertion(
                testMethod,
                AssertEnum.ASSERT_EQUALS,
                Arrays.asList(
                        this.launcher.getFactory().createThisAccess(testClass.getReference()),
                        this.launcher.getFactory().createThisAccess(testClass.getReference())
                )
        );
        assertEquals(ctInvocation.toString(), nameOfExpectedAssertClass + "assertThat(this).isEqualTo(this)", ctInvocation.toString());

        TestFramework.get().generateAfterClassToSaveObservations(testClass, Collections.singletonList(testMethod));
        assertTrue(!testClass.getMethodsByName("afterClass").isEmpty());
    }
}
