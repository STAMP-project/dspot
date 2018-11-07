package eu.stamp_project.test_framework;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.test_framework.junit.JUnit3Support;
import eu.stamp_project.test_framework.junit.JUnit4Support;
import eu.stamp_project.test_framework.junit.JUnit5Support;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class TestFrameworkFactoryTest extends AbstractTest {

    @Test
    public void testCreateCorrectTestFrameworkSupport() throws Throwable {

        /*
            Test that the TestFrameworkFactory returns the correct TestFrameworkSupport according to the test class
         */

        assertSame(JUnit3Support.class,
                TestFrameworkFactory.getTestFrameworkSupport(Utils.findClass("fr.inria.testframework.TestSupportJUnit3")).getClass()
        );
        assertSame(JUnit4Support.class,
                TestFrameworkFactory.getTestFrameworkSupport(Utils.findClass("fr.inria.testframework.TestSupportJUnit4")).getClass()
        );
        assertSame(JUnit5Support.class,
                TestFrameworkFactory.getTestFrameworkSupport(Utils.findClass("fr.inria.testframework.TestSupportJUnit5")).getClass()
        );
    }

}
