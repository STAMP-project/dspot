package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.Utils;
import fr.inria.AbstractTest;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.stamp.Main;
import org.junit.After;
import org.junit.Before;
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

	@Override
	@Before
	public void setUp() throws Exception {
		Main.verbose = true;
		DSpotUtils.withComment = false;
		AmplificationHelper.setTimeOutInMs(10000);
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		Main.verbose = false;
		DSpotUtils.withComment = false;
		AmplificationHelper.setTimeOutInMs(10000);
	}

	@Test
	public void testOnInfiniteLoop() throws Exception {
		AmplificationHelper.setTimeOutInMs(1000);
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
		final String expectedMethodWithAssertions = "@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR  +
				"public void test() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR  +
				"    final fr.inria.multipleobservations.ClassToBeTest classToBeTest = new fr.inria.multipleobservations.ClassToBeTest();" + AmplificationHelper.LINE_SEPARATOR  +
				"    org.junit.Assert.assertEquals(0, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt())));" + AmplificationHelper.LINE_SEPARATOR  +
				"    org.junit.Assert.assertEquals(0, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInteger())));" + AmplificationHelper.LINE_SEPARATOR  +
				"    classToBeTest.method();" + AmplificationHelper.LINE_SEPARATOR  +
				"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt())));" + AmplificationHelper.LINE_SEPARATOR  +
				"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInteger())));" + AmplificationHelper.LINE_SEPARATOR  +
				"}";
		assertEquals(expectedMethodWithAssertions, test_buildNewAssert.get(0).toString());
	}

	@Test
    public void testBuildAssertOnSpecificCases() throws Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted");
        MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted", "test1");
        List<CtMethod<?>> test1_buildNewAssert = mag.generateAsserts(testClass, Collections.singletonList(test1));

        final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR  +
				"    int a = 0;" + AmplificationHelper.LINE_SEPARATOR  +
				"    int b = 1;" + AmplificationHelper.LINE_SEPARATOR  +
				"    int o_test1__3 = new java.util.Comparator<java.lang.Integer>() {" + AmplificationHelper.LINE_SEPARATOR  +
				"        @java.lang.Override" + AmplificationHelper.LINE_SEPARATOR  +
				"        public int compare(java.lang.Integer integer, java.lang.Integer t1) {" + AmplificationHelper.LINE_SEPARATOR  +
				"            return integer - t1;" + AmplificationHelper.LINE_SEPARATOR  +
				"        }" + AmplificationHelper.LINE_SEPARATOR  +
				"    }.compare(a, b);" + AmplificationHelper.LINE_SEPARATOR  +
				"    org.junit.Assert.assertEquals(-1, ((int) (o_test1__3)));" + AmplificationHelper.LINE_SEPARATOR  +
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
	}

	@Test
	public void testAssertsOnMaps() throws Exception {
		CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
		MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
		CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test3");
		List<CtMethod<?>> test1_buildNewAssert = mag.generateAsserts(testClass, Collections.singletonList(test1));
		assertEquals(expectedBodyWithMap, test1_buildNewAssert.get(0).getBody().toString());
	}

	private static final String expectedBodyWithMap = "{" + AmplificationHelper.LINE_SEPARATOR +
			"    final fr.inria.sample.ClassWithMap classWithMap = new fr.inria.sample.ClassWithMap();"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key1\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"value1\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key1\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key2\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"value2\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key2\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key3\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"value3\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key3\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getEmptyMap().isEmpty());"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    classWithMap.getFullMap();" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key1\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"value1\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key1\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key2\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"value2\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key2\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key3\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"value3\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key3\"));"
			+ AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getEmptyMap().isEmpty());"
			+ AmplificationHelper.LINE_SEPARATOR +
			"}";

	@Test
	public void testMakeFailureTest() throws Exception {
		CtClass<?> testClass = Utils.findClass("fr.inria.filter.failing.FailingTest");
		MethodsAssertGenerator mag = new MethodsAssertGenerator(testClass, Utils.getInputConfiguration(), Utils.getCompiler());
		final CtMethod testAssertionError = Utils.findMethod("fr.inria.filter.failing.FailingTest", "testAssertionError");
		final List<CtMethod<?>> generatedAssertion = mag.generateAsserts(testClass, Collections.singletonList(testAssertionError));
		assertTrue(generatedAssertion.isEmpty());
	}

	private static final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR +
			"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + AmplificationHelper.LINE_SEPARATOR +
			"    cl.getFalse();" + AmplificationHelper.LINE_SEPARATOR +
			"    cl.getBoolean();" + AmplificationHelper.LINE_SEPARATOR +
			"    java.io.File file = new java.io.File(\"\");" + AmplificationHelper.LINE_SEPARATOR +
			"    boolean var = cl.getTrue();" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + AmplificationHelper.LINE_SEPARATOR +
			"}";

	private final static String expectedBodyWithComment = "{" + AmplificationHelper.LINE_SEPARATOR +
			"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + AmplificationHelper.LINE_SEPARATOR +
			"    cl.getFalse();" + AmplificationHelper.LINE_SEPARATOR +
			"    cl.getBoolean();" + AmplificationHelper.LINE_SEPARATOR +
			"    java.io.File file = new java.io.File(\"\");" + AmplificationHelper.LINE_SEPARATOR +
			"    boolean var = cl.getTrue();" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1L, ((long) (((fr.inria.sample.ClassWithBoolean)cl).getLong())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(\"this.is.a.string\", ((fr.inria.sample.ClassWithBoolean)cl).getString());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals('a', ((char) (((fr.inria.sample.ClassWithBoolean)cl).getChar())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"a\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getListWithElements().contains(\"b\"));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0, ((double) (((fr.inria.sample.ClassWithBoolean)cl).getDouble())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertNull(((fr.inria.sample.ClassWithBoolean)cl).getNull());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((byte) (((fr.inria.sample.ClassWithBoolean)cl).getByte())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((int) (((fr.inria.sample.ClassWithBoolean)cl).getInt())));" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1.0F, ((float) (((fr.inria.sample.ClassWithBoolean)cl).getFloat())), 0.1);" + AmplificationHelper.LINE_SEPARATOR +
			"    // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
			"    org.junit.Assert.assertEquals(1, ((short) (((fr.inria.sample.ClassWithBoolean)cl).getShort())));" + AmplificationHelper.LINE_SEPARATOR +
			"}";


}
