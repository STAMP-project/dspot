package eu.stamp_project.dspot;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.Collections;

import static eu.stamp_project.utils.CloneHelper.prepareTestMethod;
import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/05/18
 */
public class AmplificationTest extends AbstractTest {

    // TODO this test should be in TestCompiler
    @Ignore
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
//        final TestListener testListener = amplification.compileAndRunTestsNoFail(testClass,
//                Arrays.asList(Utils.findMethod(testClass, "test"), Utils.findMethod(testClass, "test2"))
//        );
//        assertNotNull(testListener);
//        assertTrue(testListener.getFailingTests().isEmpty());
//        assertEquals(4, testListener.getRunningTests().size());
//        assertEquals(4, testListener.getPassingTests().size());
    }

    /**
     * Tests that original test annotations are kept and modified correctly.
     */
    @Test
    public void prepareTestMethodTest() throws Exception {
        CtMethod testMethodWithAnnotations = ((CtMethod) (Utils.findClass("fr.inria.filter.passing.PassingTest").getMethodsByName("testNPEExpected").get(0)));
        final Factory factory = testMethodWithAnnotations.getFactory();
        prepareTestMethod(testMethodWithAnnotations, factory);
        // expected was kept and timeout was increased
        assertEquals("[@org.junit.Test(timeout = 10000)]",
                testMethodWithAnnotations.getAnnotations().toString());
    }
}
