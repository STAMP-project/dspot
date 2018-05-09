package fr.inria.diversify.dspot;

import eu.stamp.project.testrunner.runner.test.TestListener;
import fr.inria.AbstractTest;
import fr.inria.Utils;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/05/18
 */
public class AmplificationTest extends AbstractTest {

    @Test
    public void compileAndRunTest() throws Exception {

        /*
            Test that the Amplification can run AbstractTestClass.
            The amplification will use ALL the implementation of the AbstractTestClass
         */

        final Amplification amplification = new Amplification(
                Utils.getInputConfiguration(),
                Collections.emptyList(),
                null,
                Utils.getCompiler()
        );
        final CtClass testClass = Utils.findClass("fr.inria.inheritance.Inherited");
        final TestListener testListener = amplification.compileAndRunTestsNoFail(testClass,
                Arrays.asList(Utils.findMethod(testClass, "test"), Utils.findMethod(testClass, "test2"))
        );
        assertNotNull(testListener);
        assertTrue(testListener.getFailingTests().isEmpty());
        assertEquals(4, testListener.getRunningTests().size());
        assertEquals(4, testListener.getPassingTests().size());
    }
}
