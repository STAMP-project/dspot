package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.AbstractTest;
import org.junit.Ignore;
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
@Deprecated
public class AssertGeneratorTest extends AbstractTest {

    @Test
    @Ignore
    public void testGenerateAssert() throws Exception {

        /*
			DSpot is able to generate multiple assertion using getter inside the targeted class
				- Boolean (assertTrue / assertFalse)
				- primitive type and String (assertEquals)
				- null value (assertNull)
				- Collection: with elements (assertTrue(contains())) and empty (assertTrue(isEmpty()))
				//TODO support generation of assertion on array and on map
				//TODO DUPLICATE MethodsAssertGeneratorTest#with testBuildNewAssert
		 */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        AssertGenerator assertGenerator = new AssertGenerator(Utils.getInputConfiguration(), Utils.getCompiler());
        CtType<?> ctType = AmplificationHelper.createAmplifiedTest(assertGenerator.generateAsserts(testClass), testClass);
        assertEquals(expectedBody, ((CtMethod)ctType.getMethodsByName("test1").stream().findFirst().get()).getBody().toString());
    }

    private final static String expectedBody = "{" + nl +
            "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + nl +
            "    cl.getFalse();" + nl +
            "    cl.getBoolean();" + nl +
            "    java.io.File file = new java.io.File(\"\");" + nl +
            "    boolean var = cl.getTrue();" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())));" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + nl +
            "}";

}
