package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.util.FileUtils;
import org.junit.AfterClass;
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
public class AssertGeneratorTest {

    @Test
    public void testGenerateAssert() throws Exception, InvalidSdkException {

        /*
            test the generation of assertion
         */

        CtClass testClass = fr.inria.diversify.Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        AssertGenerator assertGenerator = new AssertGenerator(fr.inria.diversify.Utils.getInputProgram(), fr.inria.diversify.Utils.getCompiler(), fr.inria.diversify.Utils.getApplicationClassLoader());

        CtType ctType = assertGenerator.generateAsserts(testClass);

        String nl = System.getProperty("line.separator");

        final String expectedBody ="{"+ nl  +
                "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();"+ nl  +
                "    boolean o_test1__3 = cl.getFalse();"+ nl  +
                "    junit.framework.Assert.assertFalse(o_test1__3);"+ nl  +
                "    boolean o_test1__4 = cl.getBoolean();"+ nl  +
                "    junit.framework.Assert.assertTrue(o_test1__4);"+ nl  +
                "    boolean var = cl.getTrue();"+ nl  +
                "}";

        assertEquals(expectedBody, ((CtMethod)ctType.getMethods().stream().findFirst().get()).getBody().toString());
    }


    @AfterClass
    public static void tearDown() throws InvalidSdkException, Exception {
        FileUtils.forceDelete(Utils.getCompiler().getBinaryOutputDirectory());
        FileUtils.forceDelete(Utils.getCompiler().getSourceOutputDirectory());
        Utils.reset();
    }
}
