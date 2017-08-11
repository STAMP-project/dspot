package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

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
				"    boolean o_test1__3 = cl.getFalse();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test1__3, \"o_test1__3\", \"test1__3\");" + nl  +
				"    boolean o_test1__4 = cl.getBoolean();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test1__4, \"o_test1__4\", \"test1__4\");" + nl  +
				"    boolean var = cl.getTrue();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(var, \"var\", \"test1__5\");" + nl  +
				"}";
		assertEquals(expectedMethod, testWithLog.toString());
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
				"    boolean o_test2__3 = cl.getFalse();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test2__3, \"o_test2__3\", \"test2__3\");" + nl  +
				"    boolean o_test2__4 = cl.getFalse();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test2__4, \"o_test2__4\", \"test2__4\");" + nl  +
				"    boolean o_test2__5 = cl.getFalse();" + nl  +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test2__5, \"o_test2__5\", \"test2__5\");" + nl  +
				"}";
		assertEquals(expectedMethod, testWithLog.toString());
	}

}
