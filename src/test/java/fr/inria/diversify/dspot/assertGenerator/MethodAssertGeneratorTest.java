package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.AmplifierTest;
import fr.inria.diversify.testRunner.JunitResult;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:15
 */
public class MethodAssertGeneratorTest extends AmplifierTest {

    @Test
    public void testCreateTestWithoutAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler());//, Utils.getCompiler(), Utils.getApplicationClassLoader());

        mag.test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test1");
        CtMethod test1 = mag.createTestWithoutAssert(mag.test, Collections.EMPTY_LIST);
        assertEquals(expectedBodyTest1, test1.getBody().toString());
    }

    private static final String nl = System.lineSeparator();

    private static final String expectedBodyTest1 = "{" + nl +
            "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
            "    // MethodAssertGenerator build local variable" + nl +
            "    Object o_3_0 = cl.getTrue();" + nl +
            "}";

    @Test
    public void testRemoveFailAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler());//, Utils.getCompiler(), Utils.getApplicationClassLoader());

        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test1");
        mag.test = test1;

        CtMethod test1_RFA = mag.removeFailAssert();
        assertEquals(test1.getBody().toString(), test1_RFA.getBody().toString());

        mag.test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test2");
        CtMethod test2_RFA = mag.removeFailAssert();
        assertEquals(expectedBodyTest2, test2_RFA.getBody().toString());
    }

    private static final String expectedBodyTest2 = "{" + nl +
            "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
            "    org.junit.Assert.assertTrue(cl.getTrue());" + nl +
            "    // MethodAssertGenerator build local variable" + nl +
            "    Object o_5_0 = cl.getFalse();" + nl +
            "}";

    @Test
    public void testMakeFailureTest() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler());//, Utils.getCompiler(), Utils.getApplicationClassLoader());

        CtMethod test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test3");
        mag.test = test;
        JunitResult result = mag.runVersusRandomness(test);
        CtMethod test_makeFailureTest = mag.makeFailureTest(mag.getFailure("test3", result));
        assertEquals(expectedBodyTest3, test_makeFailureTest.getBody().toString());
    }

    private static final String expectedBodyTest3 = "{" + nl +
            "    // AssertGenerator generate try/catch block with fail statement" + nl +
            "    try {" + nl +
            "        fr.inria.sample.ClassThrowException cl = new fr.inria.sample.ClassThrowException();" + nl +
            "        cl.throwException();" + nl +
            "        org.junit.Assert.fail(\"test3 should have thrown Exception\");" + nl +
            "    } catch (java.lang.Exception eee) {" + nl +
            "    }" + nl +
            "}";

    @Test
    public void testBuildNewAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler());

        String nl = System.getProperty("line.separator");

        final String expectedBody = "{" + nl +
                "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
                "    // AssertGenerator add assertion" + nl +
                "    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + nl +
                "    // AssertGenerator add assertion" + nl +
                "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + nl +
                "    // AssertGenerator add assertion" + nl +
                "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + nl +
                "    // AssertGenerator replace invocation" + nl +
                "    boolean o_test1_withoutAssert__3 = cl.getFalse();" + nl +
                "    // AssertGenerator add assertion" + nl +
                "    org.junit.Assert.assertFalse(o_test1_withoutAssert__3);" + nl +
                "    // AssertGenerator replace invocation" + nl +
                "    boolean o_test1_withoutAssert__4 = cl.getBoolean();" + nl +
                "    // AssertGenerator add assertion" + nl +
                "    org.junit.Assert.assertTrue(o_test1_withoutAssert__4);" + nl +
                "    boolean var = cl.getTrue();" + nl +
                "    // AssertGenerator add assertion" + nl +
                "    org.junit.Assert.assertTrue(var);" + nl +
                "}";

        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test1");

        mag.test = test1;
        CtMethod test1_buildNewAssert = mag.generateAssert(test1);
        assertEquals(expectedBody, test1_buildNewAssert.getBody().toString());
    }

}
