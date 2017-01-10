package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.dspot.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import static org.junit.Assert.assertEquals;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/8/16
 */
public class AssertGeneratorTest extends AbstractTest {

    @Test
    public void testGenerateAssert() throws Exception, InvalidSdkException {

        /*
            test the generation of assertion
         */

        CtClass testClass = fr.inria.diversify.Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        System.out.println(testClass);
        AssertGenerator assertGenerator = new AssertGenerator(fr.inria.diversify.Utils.getInputProgram(), fr.inria.diversify.Utils.getCompiler(), fr.inria.diversify.Utils.getApplicationClassLoader());
        CtType ctType = AmplificationHelper.createAmplifiedTest(assertGenerator.generateAsserts(testClass), testClass);
        System.out.println(((CtMethod)ctType.getMethods().stream().findFirst().get()).getBody().toString());
        assertEquals(expectedBody, ((CtMethod)ctType.getMethods().stream().findFirst().get()).getBody().toString());
    }

    private static String nl = System.getProperty("line.separator");

    private static final String expectedBody = "{" + nl  +
            "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl  +
            "    // AssertGenerator replace invocation" + nl  +
            "    boolean o_test1__3 = cl.getFalse();" + nl  +
            "    // AssertGenerator add assertion" + nl  +
            "    org.junit.Assert.assertFalse(o_test1__3);" + nl  +
            "    // AssertGenerator replace invocation" + nl  +
            "    boolean o_test1__4 = cl.getBoolean();" + nl  +
            "    // AssertGenerator add assertion" + nl  +
            "    org.junit.Assert.assertTrue(o_test1__4);" + nl  +
            "    boolean var = cl.getTrue();" + nl  +
            "}";

}
