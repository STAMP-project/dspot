package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.testRunner.JunitResult;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;

import static fr.inria.diversify.dspot.assertGenerator.AssertCt.assertBodyEquals;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:15
 */
public class MethodAssertGeneratorTest {

    @Test
    public void testCreateTestWithoutAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());

        CtMethod testWithoutAssert = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test1_withoutAssert");

        mag.test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test1");
        CtMethod test1 = mag.createTestWithoutAssert(new ArrayList<>(), false);
        assertBodyEquals(testWithoutAssert, test1);

        mag.test = testWithoutAssert;
        CtMethod test2 = mag.createTestWithoutAssert(new ArrayList<>(), false);
        assertBodyEquals(testWithoutAssert, test2);
    }

    @Test
    public void testRemoveFailAssert() throws InvalidSdkException, Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());

       CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test1");
        mag.test = test1;

        CtMethod test1_RFA = mag.removeFailAssert();
        assertBodyEquals(test1_RFA, test1);

        CtMethod test2_RemoveFailAssert = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test2_RemoveFailAssert");
        mag.test = Utils.findMethod("fr.inria.sample.TestClassWithAssert", "test2");

        CtMethod test2_RFA = mag.removeFailAssert();
        assertBodyEquals(test2_RemoveFailAssert, test2_RFA);
    }

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
        CtClass testClass = Utils.findClass("fr.inria.sample.TestCassWithoutAssert");
        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());

        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestCassWithoutAssert", "test1");
        CtMethod test1_withAssert = Utils.findMethod("fr.inria.sample.TestCassWithoutAssert", "test1_withAssert");
        mag.test = test1;
        CtMethod test1_buildNewAssert = mag.generateAssert(test1);
         assertBodyEquals(test1_buildNewAssert, test1_withAssert);



    }
}
