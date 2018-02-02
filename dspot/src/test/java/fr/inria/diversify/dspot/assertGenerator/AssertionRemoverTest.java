package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.utils.AmplificationChecker;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/06/17
 */
public class AssertionRemoverTest extends AbstractTest {

	@Test
	public void testRemoveAssertionOnSimpleExample() throws Exception {
		final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
		final AssertionRemover assertionRemover = new AssertionRemover();
		testClass.getMethodsByName("test1").get(0).getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation element) {
				return AmplificationChecker.isAssert(element);
			}
		}).forEach(assertionRemover::removeAssertion);

		final String expectedMethod = "@org.junit.Test" + nl +
				"public void test1() {" + nl +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
				"    cl.getTrue();" + nl +
				"}";
		assertEquals(expectedMethod , testClass.getMethodsByName("test1").get(0).toString());
	}

	@Test
	public void testRemoveAssertionWithSwitchCase() throws Exception {

		/*
			Test that the AssertionRemover remove assertions from tests when the assertion is inside a case:
		 */

		final CtClass<?> testClass = Utils.findClass("fr.inria.assertionremover.TestClassWithAssertToBeRemoved");
		final AssertionRemover assertionRemover = new AssertionRemover();
		testClass.getMethodsByName("test1").get(0).getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation element) {
				return AmplificationChecker.isAssert(element);
			}
		}).forEach(assertionRemover::removeAssertion);

		final String expectedMethod = "@org.junit.Test" + nl +
				"public void test1() {" + nl +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
				"    cl.getTrue();" + nl +
				"    int one = 1;" + nl +
				"    switch (one) {" + nl +
				"        case 1 :" + nl +
				"            fr.inria.assertionremover.TestClassWithAssertToBeRemoved.getNegation(cl.getFalse());" + nl +
				"            break;" + nl +
				"    }" + nl +
				"}";
		assertEquals(expectedMethod , testClass.getMethodsByName("test1").get(0).toString());
	}

	@Test
	public void testRemoveAssertionWithUnary() throws Exception {

		/*
			Test that the AssertionRemover remove assertions from tests when assertions used unary operators
		 */

		final CtClass<?> testClass = Utils.findClass("fr.inria.assertionremover.TestClassWithAssertToBeRemoved");
		final AssertionRemover assertionRemover = new AssertionRemover();
		testClass.getMethodsByName("test2").get(0).getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation element) {
				return AmplificationChecker.isAssert(element);
			}
		}).forEach(assertionRemover::removeAssertion);

		final String expectedMethod = "@org.junit.Test" + nl +
				"public void test2() throws java.lang.Exception {" + nl +
				"    int a = -1;" + nl +
				"    int b = 1;" + nl +
				"}";
		assertEquals(expectedMethod , testClass.getMethodsByName("test2").get(0).toString());
	}

	@Test
	public void testOnDifferentKindOfAssertions() throws Exception {
		/*
			Test that the AssertionRemover remove all kind of assertions
		 */

		final CtClass<?> testClass = Utils.findClass("fr.inria.helper.TestWithMultipleAsserts");
		final AssertionRemover assertionRemover = new AssertionRemover();
		final CtMethod<?> testMethod = testClass.getMethodsByName("test").get(0);
		testMethod.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation element) {
				return AmplificationChecker.isAssert(element);
			}
		}).forEach(assertionRemover::removeAssertion);
		assertTrue(testMethod.toString() + " its body should be empty",
				testMethod.getBody().getStatements().isEmpty()
		);
		System.out.println(testMethod);
	}
}
