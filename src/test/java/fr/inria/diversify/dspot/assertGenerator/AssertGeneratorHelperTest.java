package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

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

	/**
	 * this test aims at verifying that dspot does not generate assertion for generated object.
	 * To do this, it will check that the instrumentation does not add observation points on those objects.
	 * If no observation point is added, any assertion would be generated.
	 */
	@Test
	public void testNoInstrumentationOnGeneratedObject() throws Exception {
		final String packageName = "fr.inria.statementaddarray";
		InputProgram inputProgram = Utils.getInputProgram();
		final Factory factory = inputProgram.getFactory();
		inputProgram.setFactory(factory);
		AmplificationHelper.setSeedRandom(32L);
		StatementAdd amplifier = new StatementAdd(packageName);
		amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));

		CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
		List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

		final List<CtMethod<?>> instrumentedAmplifiedTests = amplifiedMethods.stream()
				.map(method -> AssertGeneratorHelper.createTestWithLog(method, "fr.inria.statementaddarray"))
				.collect(Collectors.toList());

		assertEquals(5, amplifiedMethods.size());

		final String expectedInstrumentedBodyAfterAmplification_test_sd6_withlog = "{" + nl +
				"    fr.inria.statementaddarray.ClassTargetAmplify clazz = new fr.inria.statementaddarray.ClassTargetAmplify();" + nl +
				"    fr.inria.diversify.compare.ObjectLog.log(clazz, \"clazz\", \"test_sd6__1\");" + nl +
				"    // StatementAdd: generate variable from return value" + nl +
				"    fr.inria.statementaddarray.ClassParameterAmplify __DSPOT_invoc_3 = clazz.methodWithReturn();" + nl +
				"    // StatementAdd: add invocation of a method" + nl +
				"    __DSPOT_invoc_3.method1();" + nl +
				"    fr.inria.diversify.compare.ObjectLog.log(clazz, \"clazz\", \"test_sd6__1___end\");" + nl +
				"}";

		assertEquals(expectedInstrumentedBodyAfterAmplification_test_sd6_withlog,
				instrumentedAmplifiedTests.stream()
						.filter(ctMethod1 ->
								"test_sd6_withlog".equals(ctMethod1.getSimpleName())
						).findFirst()
						.get()
						.getBody()
						.toString());

	}

	@Test
	public void testCreateTestWithLogClassTargetAmplify() throws Exception {
		final CtMethod<?> test1 = Utils.findMethod("fr.inria.statementaddarray.TestClassTargetAmplify", "test");
		final CtMethod<?> testWithLog =
				AssertGeneratorHelper.createTestWithLog(test1,"fr.inria.statementaddarray");
		System.out.println(testWithLog);
	}

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
