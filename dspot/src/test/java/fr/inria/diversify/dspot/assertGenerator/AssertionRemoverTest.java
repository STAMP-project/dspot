package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.Utils;
import fr.inria.AbstractTest;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import static org.junit.Assert.assertEquals;


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

		final String expectedMethod = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
				"public void test1() {" + AmplificationHelper.LINE_SEPARATOR +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
				"    cl.getTrue();" + AmplificationHelper.LINE_SEPARATOR +
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

		final String expectedMethod = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
				"public void test1() {" + AmplificationHelper.LINE_SEPARATOR +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
				"    cl.getTrue();" + AmplificationHelper.LINE_SEPARATOR +
				"    int one = 1;" + AmplificationHelper.LINE_SEPARATOR +
				"    switch (one) {" + AmplificationHelper.LINE_SEPARATOR +
				"        case 1 :" + AmplificationHelper.LINE_SEPARATOR +
				"            fr.inria.assertionremover.TestClassWithAssertToBeRemoved.getNegation(cl.getFalse());" + AmplificationHelper.LINE_SEPARATOR +
				"            break;" + AmplificationHelper.LINE_SEPARATOR +
				"    }" + AmplificationHelper.LINE_SEPARATOR +
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

		final String expectedMethod = "@org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
				"public void test2() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
				"    int a = -1;" + AmplificationHelper.LINE_SEPARATOR +
				"    int b = 1;" + AmplificationHelper.LINE_SEPARATOR +
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
		final CtMethod<?> removedAssertion = assertionRemover.removeAssertion(testMethod);
		System.out.println(removedAssertion);
		assertEquals(2, removedAssertion.getBody().getStatements().size());
	}
}
