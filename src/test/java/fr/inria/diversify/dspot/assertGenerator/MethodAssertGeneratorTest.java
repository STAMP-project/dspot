package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Collections;

import static fr.inria.diversify.dspot.assertGenerator.AssertCt.assertBodyEquals;
import static org.junit.Assert.assertEquals;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:15
 */
public class MethodAssertGeneratorTest extends AbstractTest {

    @Test
    public void testCreateTestWithoutAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());

        mag.test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test1");
        CtMethod test1 = mag.createTestWithoutAssert(Collections.EMPTY_LIST, false);
        assertEquals(expectedBodyTest1, test1.getBody().toString());
    }

    private static final String nl = System.lineSeparator();

    private static final String expectedBodyTest1 = "{" + nl +
            "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
            "    Object o_3_0 = cl.getTrue();" + nl +
            "}";

    @Test
    public void testRemoveFailAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());

        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test1");
        mag.test = test1;

        CtMethod test1_RFA = mag.removeFailAssert();
        assertEquals(test1.getBody().toString(), test1_RFA.getBody().toString());

        mag.test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test2");
        CtMethod test2_RFA = mag.removeFailAssert();
        assertEquals(expectedBodyTest2, test2_RFA.getBody().toString());
    }

    private static final String expectedBodyTest2 = "{" + nl  +
            "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl  +
            "    org.junit.Assert.assertTrue(cl.getTrue());" + nl  +
            "    Object o_5_0 = cl.getFalse();" + nl  +
            "}";

    @Test
    public void testMakeFailureTest() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());

        CtMethod test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test3");
        mag.test = test;
        JunitResult result = mag.runSingleTest(test);
        CtMethod test_makeFailureTest = mag.makeFailureTest(mag.getFailure("test3", result));
        CtMethod test_exceptionCatch = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test3_exceptionCatch");
        assertBodyEquals(test_makeFailureTest, test_exceptionCatch);
    }

    @Test
    public void testBuildNewAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());

        String nl = System.getProperty("line.separator");

        final String expectedBody = "{" + nl +
                "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
                "    junit.framework.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + nl +
                "    junit.framework.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + nl +
                "    junit.framework.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + nl +
                "    boolean o_test1_withoutAssert__3 = cl.getFalse();" + nl +
                "    junit.framework.Assert.assertFalse(o_test1_withoutAssert__3);" + nl +
                "    boolean o_test1_withoutAssert__4 = cl.getBoolean();" + nl +
                "    junit.framework.Assert.assertTrue(o_test1_withoutAssert__4);" + nl +
                "    boolean var = cl.getTrue();" + nl +
                "    junit.framework.Assert.assertTrue(var);" + nl +
                "}";

        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test1");

        mag.test = test1;
        CtMethod test1_buildNewAssert = mag.generateAssert(test1);
        assertEquals(expectedBody, test1_buildNewAssert.getBody().toString());
    }

}
