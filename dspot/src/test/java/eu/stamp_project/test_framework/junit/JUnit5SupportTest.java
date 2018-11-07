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
public class JUnit5SupportTest extends AbstractTest {

    @Test
    public void test() throws Throwable {
        final CtClass<?> junitTest4 = Utils.findClass("fr.inria.testframework.TestSupportJUnit4");
        final CtClass<?> junitTest5 = Utils.findClass("fr.inria.testframework.TestSupportJUnit5");
        final TestFrameworkSupport testFrameworkSupport = TestFrameworkFactory.getTestFrameworkSupport(junitTest5);
        assertTrue(testFrameworkSupport.isTest(junitTest5.getMethods().stream().findFirst().get()));
        assertFalse(testFrameworkSupport.isTest(junitTest4.getMethods().stream().findFirst().get()));
    }
}
