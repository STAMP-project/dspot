package fr.inria.diversify.dspot.testrunner;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.assertGenerator.MethodAssertGenerator;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.TestCompiler;
import fr.inria.diversify.testRunner.TestRunner;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.FileNotFoundException;

import static fr.inria.diversify.util.FileUtils.forceDelete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/15/17
 */
public class TestRunnerTest {

    @Before
    public void setUp() throws Exception {
        Utils.init("src/test/resources/sample/sample.properties");
        try {
            forceDelete(Utils.getCompiler().getBinaryOutputDirectory());
        } catch (FileNotFoundException | IllegalArgumentException ignored) {
        }
        try {
            forceDelete(Utils.getCompiler().getSourceOutputDirectory());
        } catch (FileNotFoundException | IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testVisibility() throws Exception, InvalidSdkException {

        /*
            Test that the Method assert generator build a test with right visibility to not add
                a wrong try/catch block with fail assertion at the end.
         */

        CtClass testClass = Utils.findClass("fr.inria.testrunner.TestClassWithVisibility");
        String classpath = Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getClassesDir() + ":" +
                Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir();
        boolean status = TestCompiler.writeAndCompile(Utils.getCompiler(), testClass, false, classpath);
        if (!status) {
            throw new RuntimeException("Error compilation");
        }
        classpath = AmplificationHelper.getClassPath(Utils.getCompiler(), Utils.getInputProgram());
        JunitResult result = TestRunner.runTests(testClass, testClass.getMethodsByName("testFromCommonsLang"), classpath, Utils.getInputConfiguration());

        assertEquals(0, result.getFailures().size());
        assertEquals(1, result.getTestsRun().size());

        MethodAssertGenerator mag = new MethodAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
        result = mag.runTests(testClass, testClass.getMethodsByName("testFromCommonsLang"));
        assertEquals(0, result.getFailures().size());
        assertEquals(1, result.getTestsRun().size());

        CtMethod originalTestCase = Utils.findMethod(testClass, "testFromCommonsLang");
        CtMethod amplifiedTestCase = mag.generateAssert(originalTestCase);
        assertTrue(null != amplifiedTestCase);
        assertTrue(amplifiedTestCase.getElements(new TypeFilter<CtTry>(CtTry.class)).isEmpty());
    }
}
