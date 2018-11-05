package eu.stamp_project.dspot;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import static eu.stamp_project.utils.CloneHelper.prepareTestMethod;
import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/05/18
 */
public class AmplificationTest extends AbstractTest {

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
