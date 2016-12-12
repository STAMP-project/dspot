package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import fr.inria.diversify.dspot.AmplificationHelper;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/7/16
 */
public class TestMethodCallRemove {

    @Test
    public void testMethodCallRemoveAll() throws Exception, InvalidSdkException {

        /*
            Test that we remove method call in a test for each used method in the test.
                3 method are called in the original test, we produce 3 test methods.
         */

        CtClass<Object> testClass = Utils.getFactory().Class().get("fr.inria.mutation.ClassUnderTestTest");

        TestMethodCallRemover methodCallRemove = new TestMethodCallRemover();
        methodCallRemove.reset(null, null);

        final CtMethod<?> originalMethod = testClass.getMethods().stream().filter(m -> "testAddCall".equals(m.getSimpleName())).findFirst().get();
        List<CtMethod> amplifiedMethods = methodCallRemove.apply(originalMethod);

        assertEquals(2, amplifiedMethods.size());

        for (int i = 0; i < amplifiedMethods.size(); i++) {
            assertEquals(originalMethod.getBody().getStatements().size() - 1, amplifiedMethods.get(i).getBody().getStatements().size());
            assertNotEquals(amplifiedMethods.get((i+1) % amplifiedMethods.size()).getBody(), amplifiedMethods.get(i).getBody());//checks that all generated methods are different
        }
    }

    @Test
    public void testMethodCallRemoveRnd() throws Exception, InvalidSdkException {

        /*
            Test that we remove method call in a test for each used method in the test.
                2 method are called in the original test, we produce 2 test methods randomly among them.
         */

        CtClass<Object> testClass = Utils.getFactory().Class().get("fr.inria.mutation.ClassUnderTestTest");
        AmplificationHelper.setSeedRandom(23L);

        TestMethodCallRemover methodCallRemove = new TestMethodCallRemover();
        methodCallRemove.reset(null, null);

        final CtMethod<?> originalMethod = testClass.getMethods().stream().filter(m -> "testAddCall".equals(m.getSimpleName())).findFirst().get();
        CtMethod amplifiedMethod = methodCallRemove.applyRandom(originalMethod);
        CtMethod amplifiedMethod2 = methodCallRemove.applyRandom(originalMethod);

        assertEquals(originalMethod.getBody().getStatements().size() - 1, amplifiedMethod.getBody().getStatements().size());
        assertEquals(originalMethod.getBody().getStatements().size() - 1, amplifiedMethod2.getBody().getStatements().size());
        assertNotEquals(amplifiedMethod.getBody().getStatements(), amplifiedMethod2.getBody().getStatements());
    }

    @AfterClass
    public static void tearDown() throws InvalidSdkException, Exception {
        FileUtils.forceDelete(Utils.getCompiler().getBinaryOutputDirectory());
        FileUtils.forceDelete(Utils.getCompiler().getSourceOutputDirectory());
        Utils.reset();
    }
}
