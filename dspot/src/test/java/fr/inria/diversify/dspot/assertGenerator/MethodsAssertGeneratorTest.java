package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.utils.DSpotUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/4/17
 */
public class MethodsAssertGeneratorTest extends AbstractTest {

	@Test
	public void testOnInfiniteLoop() throws Exception {
		final CtClass testClass = Utils.findClass("fr.inria.infinite.LoopTest");
		MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
		CtMethod test = Utils.findMethod("fr.inria.infinite.LoopTest", "testLoop");
		List<CtMethod<?>> test_buildNewAssert = mag.generateAsserts(testClass, Collections.singletonList(test));
		assertTrue(test_buildNewAssert.isEmpty());
	}

	@Test
	public void testMultipleObservationsPoints() throws Exception {
		CtClass testClass = Utils.findClass("fr.inria.multipleobservations.TestClassToBeTest");
		MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
		CtMethod test = Utils.findMethod("fr.inria.multipleobservations.TestClassToBeTest", "test");
		List<CtMethod<?>> test_buildNewAssert = mag.generateAsserts(testClass, Collections.singletonList(test));
		final String expectedMethodWithAssertions = "@org.junit.Test(timeout = 10000)" + nl  +
				"public void test() throws java.lang.Exception {" + nl  +
				"    final fr.inria.multipleobservations.ClassToBeTest classToBeTest = new fr.inria.multipleobservations.ClassToBeTest();" + nl  +
				"    org.junit.Assert.assertEquals(0, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt())));" + nl  +
				"    org.junit.Assert.assertEquals(0, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInteger())));" + nl  +
				"    classToBeTest.method();" + nl  +
				"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt())));" + nl  +
				"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInteger())));" + nl  +
				"}";
		assertEquals(expectedMethodWithAssertions, test_buildNewAssert.get(0).toString());
	}

	@Test
    public void testBuildAssertOnSpecificCases() throws Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted");
        MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted", "test1");
        List<CtMethod<?>> test1_buildNewAssert = mag.generateAsserts(testClass, Collections.singletonList(test1));

        final String expectedBody = "{" + nl  +
				"    int a = 0;" + nl  +
				"    int b = 1;" + nl  +
				"    int o_test1__3 = new java.util.Comparator<java.lang.Integer>() {" + nl  +
				"        @java.lang.Override" + nl  +
				"        public int compare(java.lang.Integer integer, java.lang.Integer t1) {" + nl  +
				"            return integer - t1;" + nl  +
				"        }" + nl  +
				"    }.compare(a, b);" + nl  +
				"    org.junit.Assert.assertEquals(-1, ((int) (o_test1__3)));" + nl  +
				"}";

        assertEquals(expectedBody, test1_buildNewAssert.get(0).getBody().toString());
    }

    @Test
    public void testBuildNewAssert() throws Exception {
		/*
			DSpot is able to generate multiple assertion using getter inside the targeted class
				- Boolean (assertTrue / assertFalse)
				- primitive type and String (assertEquals)
				- null value (assertNull)
				- Collection: with elements (assertTrue(contains())) and empty (assertTrue(isEmpty()))
				//TODO support generation of assertion on array and on map
		 */
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test1");
        List<CtMethod<?>> test1_buildNewAssert = mag.generateAsserts(testClass, Collections.singletonList(test1));
        assertEquals(expectedBody, test1_buildNewAssert.get(0).getBody().toString());
    }

	@Test
	public void testBuildNewAssertWithComment() throws Exception {

		/*
			Same as testBuildNewAssert but with Comment enabled
		 */

		DSpotUtils.withComment = true;

		CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
		MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
		CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test1");
		List<CtMethod<?>> test1_buildNewAssert = mag.generateAsserts(testClass, Collections.singletonList(test1));
		assertEquals(expectedBodyWithComment, test1_buildNewAssert.get(0).getBody().toString());

		DSpotUtils.withComment = false;
	}

	@Test
	public void testMakeFailureTest() throws Exception {
		CtClass<?> testClass = Utils.findClass("fr.inria.filter.failing.FailingTest");
		MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
		final CtMethod testAssertionError = Utils.findMethod("fr.inria.filter.failing.FailingTest", "testAssertionError");
		final List<CtMethod<?>> generatedAssertion = mag.generateAsserts(testClass, Collections.singletonList(testAssertionError));
		assertTrue(generatedAssertion.isEmpty());
	}

	private static final String expectedBody = "{" + nl +
			"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + nl +
			"    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + nl +
			"    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + nl +
			"    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + nl +
			"    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())));" + nl +
			"    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + nl +
			"    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + nl +
			"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + nl +
			"    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + nl +
			"    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())));" + nl +
			"    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + nl +
			"    cl.getFalse();" + nl +
			"    cl.getBoolean();" + nl +
			"    java.io.File file = new java.io.File(\"\");" + nl +
			"    boolean var = cl.getTrue();" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + nl +
			"    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + nl +
			"    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + nl +
			"    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + nl +
			"    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())));" + nl +
			"    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + nl +
			"    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + nl +
			"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + nl +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + nl +
			"    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + nl +
			"    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())));" + nl +
			"    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + nl +
			"}";

	private final static String expectedBodyWithComment = "{" + nl +
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
