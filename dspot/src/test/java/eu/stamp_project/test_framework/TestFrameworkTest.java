package eu.stamp_project.test_framework;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

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
    public void testIsAssert() {

    }
}
