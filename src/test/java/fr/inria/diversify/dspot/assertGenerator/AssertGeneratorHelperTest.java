package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class AssertGeneratorHelperTest extends AbstractTest {

	@Test
	public void testCreateTestWithLog() throws Exception {
		/*
			test the creation of test with log
         */

		CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
		final CtMethod<?> test1 = (CtMethod<?>) testClass.getMethodsByName("test1").get(0);
		final CtMethod<?> testWithLog =
				AssertGeneratorHelper.createTestWithLog(test1,"fr.inria.sample");

		final String expectedMethod = "@org.junit.Test(timeout = 10000)" + nl  +
				"public void test1_withlog() {" + nl  +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(cl, \"cl\", \"test1__1\");" + nl  +
				"    cl.getFalse();" + nl  +
				"    cl.getBoolean();" + nl  +
				"    java.io.File file = new java.io.File(\"\");" + nl +
				"    boolean var = cl.getTrue();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(cl, \"cl\", \"test1__1___end\");" + nl +
				"}";
		assertEquals(expectedMethod, testWithLog.toString());
	}

	@Test
	public void testCreateTestWithLogWithoutChainSameObservations() throws Exception {
		CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted", "test1");
		final CtMethod<?> testWithLog =
				AssertGeneratorHelper.createTestWithLog(test1,"fr.inria.sample");

		final String expectedMethodWithLog = "@org.junit.Test(timeout = 10000)\n" +
				"public void test1_withlog() {\n" +
				"    int a = 0;\n" +
				"    int b = 1;\n" +
				"    int o_test1__3 = new java.util.Comparator<java.lang.Integer>() {\n" +
				"        @java.lang.Override\n" +
				"        public int compare(java.lang.Integer integer, java.lang.Integer t1) {\n" +
				"            return integer - t1;\n" +
				"        }\n" +
				"    }.compare(a, b);\n" +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test1__3, \"o_test1__3\", \"test1__3\");\n" +
				"}";
		assertEquals(expectedMethodWithLog, testWithLog.toString());
	}

	@Test
	public void testCreateTestWithLogWithDuplicatedStatement() throws Exception {
		/*
			test the creation of log with duplicates statement
		 */
		CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
		final CtMethod<?> test2 = (CtMethod<?>) testClass.getMethodsByName("test2").get(0);
		final CtMethod<?> testWithLog =
				AssertGeneratorHelper.createTestWithLog(test2,"fr.inria.sample");

		final String expectedMethod = "@org.junit.Test(timeout = 10000)" + nl  +
				"public void test2_withlog() {" + nl  +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(cl, \"cl\", \"test2__1\");" + nl  +
				"    cl.getFalse();" + nl  +
				"    cl.getFalse();" + nl  +
				"    cl.getFalse();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(cl, \"cl\", \"test2__1___end\");" + nl +
				"}";
		assertEquals(expectedMethod, testWithLog.toString());
	}

}
