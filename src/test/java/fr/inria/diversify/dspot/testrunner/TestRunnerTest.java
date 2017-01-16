package fr.inria.diversify.dspot.testrunner;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.assertGenerator.MethodAssertGenerator;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.TestCompiler;
import fr.inria.diversify.testRunner.TestRunner;
import org.junit.Test;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/15/17
 */
public class TestRunnerTest {

    @Test
    public void testVisibility() throws Exception, InvalidSdkException {

        /*
            Test that the Method assert generator build a test with right visibility to not add
                a wrong try/catch block with fail assertion at the end.
         */

        CtClass testClass = Utils.findClass("fr.inria.testrunner.TestClassWithVisibility");
        TestCompiler.writeAndCompile(Utils.getApplicationClassLoader(), Utils.getCompiler(), testClass, false);
        JunitResult result = TestRunner.runTests(Utils.getApplicationClassLoader(), Utils.getCompiler(),
                Utils.getInputProgram().getProgramDir() + "/log", Utils.getInputProgram().getProgramDir(),
                testClass, testClass.getMethodsByName("testFromCommonsLang"),
                Collections.singleton("fr.inria.testrunner"),
                Utils.getInputProgram());
        assertEquals(0, result.getFailures().size());
        assertEquals(1, result.getTestRuns().size());

        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputProgram(), Utils.getCompiler(), Utils.getApplicationClassLoader());
        result = mag.runTests(testClass, testClass.getMethodsByName("testFromCommonsLang"));
        assertEquals(0, result.getFailures().size());
        assertEquals(1, result.getTestRuns().size());

        CtMethod originalTestCase = Utils.findMethod(testClass, "testFromCommonsLang");
        CtMethod amplifiedTestCase = mag.generateAssert(originalTestCase);
        assertTrue(null != amplifiedTestCase);
        assertTrue(amplifiedTestCase.getElements(new TypeFilter<CtTry>(CtTry.class)).isEmpty());
    }
}
