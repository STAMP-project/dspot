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
		final CtMethod<?> testWithLog = AssertGeneratorHelper.createTestWithLog(test1,
				IntStream.range(0, test1.getBody().getStatements().size())
						.boxed()
						.collect(Collectors.toList()),
				"test1"
		);

		final String expectedMethod = "@org.junit.Test\n" +
				"public void test1_withlog() {\n" +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();\n" +
				"    fr.inria.diversify.compare.ObjectLog.log(cl,\"cl\",\"test1__1\");\n" +
				"    Object o_test1__3 = cl.getFalse();\n" +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test1__3,\"o_test1__3\",\"test1__3\");\n" +
				"    Object o_test1__4 = cl.getBoolean();\n" +
				"    fr.inria.diversify.compare.ObjectLog.logObject(o_test1__4,\"o_test1__4\",\"test1__4\");\n" +
				"    boolean var = cl.getTrue();\n" +
				"    fr.inria.diversify.compare.ObjectLog.logObject(var,\"var\",\"test1__5\");\n" +
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
		final CtMethod<?> testWithLog = AssertGeneratorHelper.createTestWithLog(test2,
				IntStream.range(0, test2.getBody().getStatements().size())
						.boxed()
						.collect(Collectors.toList()),
				"test2"
		);

		final String expectedMethod = "@org.junit.Test\n" +
				"public void test2_withlog() {\n" +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();\n" +
				"    fr.inria.diversify.compare.ObjectLog.log(cl,\"cl\",\"test2__1\");\n" +
				"    Object o_test2__3 = cl.getFalse();\n" +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test2__3,\"o_test2__3\",\"test2__3\");\n" +
				"    Object o_test2__4 = cl.getFalse();\n" +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test2__4,\"o_test2__4\",\"test2__4\");\n" +
				"    Object o_test2__5 = cl.getFalse();\n" +
				"    fr.inria.diversify.compare.ObjectLog.log(o_test2__5,\"o_test2__5\",\"test2__5\");\n" +
				"}";
		assertEquals(expectedMethod, testWithLog.toString());
	}

}
