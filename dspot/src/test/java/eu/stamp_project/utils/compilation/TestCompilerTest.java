package eu.stamp_project.utils.compilation;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.program.InputConfiguration;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/13/17
 */
public class TestCompilerTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        CtClass<?> testClass = Utils.findClass("fr.inria.filter.failing.FailingTest");
        assertTrue(TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                testClass,
                Arrays.asList(
                        Utils.findMethod(testClass, "testAssertionError"),
                        Utils.findMethod(testClass, "testFailingWithException")
                ),
                Utils.getCompiler(),
                InputConfiguration.get()
        ).isEmpty());

        testClass = Utils.findClass("fr.inria.filter.passing.PassingTest");
        assertEquals(2,
                TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                        testClass,
                        Arrays.asList(
                                Utils.findMethod(testClass, "testAssertion"),
                                Utils.findMethod(testClass, "testNPEExpected"),
                                Utils.findMethod(testClass, "failingTestCase")
                        ),
                        Utils.getCompiler(),
                        InputConfiguration.get()
                ).size()
        );
    }
}
