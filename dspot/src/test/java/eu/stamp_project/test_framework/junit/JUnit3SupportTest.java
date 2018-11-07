package eu.stamp_project.test_framework.junit;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.test_framework.TestFrameworkFactory;
import eu.stamp_project.test_framework.TestFrameworkSupport;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit3SupportTest extends AbstractTest {

    @Test
    public void test() throws Throwable {
        final CtClass<?> junitTest3 = Utils.findClass("fr.inria.testframework.TestSupportJUnit3");
        final CtClass<?> junitTest4 = Utils.findClass("fr.inria.testframework.TestSupportJUnit4");
        final TestFrameworkSupport testFrameworkSupport = TestFrameworkFactory.getTestFrameworkSupport(junitTest3);
        assertTrue(testFrameworkSupport.isTest(junitTest3.getMethods().stream().findFirst().get()));
        assertFalse(testFrameworkSupport.isTest(junitTest4.getMethods().stream().findFirst().get()));
    }
}
