package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
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

    @BeforeClass
    public static void beforeClass() throws Exception {
        Utils.reset();
    }

    @Test
    public void testOnTestMethodWithNonJavaIdentifier() throws Exception {

        /*
            test that we can remove assert that have type that are not correct java identifier
         */

        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithCatchVariable = Utils.findMethod(testClass, "testWithArray");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(testWithCatchVariable);
        assertTrue(
                ctMethod.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class))
                        .stream()
                        .map(CtLocalVariable::getSimpleName)
                        .allMatch(string -> {
                                    try {
                                        for (int i = 0; i < string.length(); i++) {
                                            if (!Character.isJavaIdentifierPart(string.charAt(i))) {
                                                return false;
                                            }
                                        }
                                        return true;
                                    } catch (Exception ignored) {
                                        return false;
                                    }
                                }
                        )
        );

    }

    @Test
    public void testOnCatchVariable() throws Exception {
        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> testWithCatchVariable = Utils.findMethod(testClass, "testWithCatchVariable");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> ctMethod = assertionRemover.removeAssertion(testWithCatchVariable);
        assertTrue(
                ctMethod.getElements(new TypeFilter<>(CtCatch.class))
                        .get(0)
                        .getBody()
                        .getStatements().isEmpty());
    }

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
        assertEquals(expectedMethod, testClass.getMethodsByName("test1").get(0).toString());
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
        assertEquals(expectedMethod, testClass.getMethodsByName("test1").get(0).toString());
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
        assertEquals(expectedMethod, testClass.getMethodsByName("test2").get(0).toString());
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
        assertEquals(4, removedAssertion.getBody().getStatements().size());
    }
}
