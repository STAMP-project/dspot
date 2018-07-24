package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/4/17
 */
public class AssertGeneratorTest extends AbstractTest {

    private AssertGenerator assertGenerator;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.assertGenerator = new AssertGenerator(Utils.getInputConfiguration(), Utils.getCompiler());
    }

    @After
    public void tearDown() throws Exception {
        Utils.getInputConfiguration().setWithComment(false);
        Utils.getInputConfiguration().setTimeOutInMs(10000);
    }

    @Test
    public void testCreateLogOnClassObject() throws Exception {
        final CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        final CtMethod<?> testOnClass = Utils.findMethod(testClass, "testOnClass");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> testWithoutAssertions = assertionRemover.removeAssertion(testOnClass);
        System.out.println(
                assertGenerator.assertionAmplification(testClass, Collections.singletonList(testWithoutAssertions))
        );
    }

    @Test
    public void testOnInfiniteLoop() throws Exception {
        Utils.getInputConfiguration().setTimeOutInMs(1000);
        final CtClass testClass = Utils.findClass("fr.inria.infinite.LoopTest");
        CtMethod test = Utils.findMethod("fr.inria.infinite.LoopTest", "testLoop");
        List<CtMethod<?>> test_buildNewAssert = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test));
        assertTrue(test_buildNewAssert.isEmpty());
    }

    private static final String ASSERT_EQUALS = "assertEquals";
    private static final String ASSERT_TRUE = "assertTrue";
    private static final String ASSERT_FALSE= "assertFalse";
    private static final String ASSERT_NULL= "assertNull";

    /**
     * This class is used to verify that the method, with the given name, is inside an assertion
     */
    public class AssertionFilterNameOnInvocation extends TypeFilter<CtInvocation<?>> {

        public AssertionFilterNameOnInvocation(String nameOfMethodThatMustBeInsideTheAssertion, String assertionName) {
            super(CtInvocation.class);
            this.assertionName = assertionName;
            this.namedElementFilter = new NamedElementFilter<>(CtNamedElement.class, nameOfMethodThatMustBeInsideTheAssertion);
            this.referenceFilter = new TypeFilter<CtReference>(CtReference.class) {
                @Override
                public boolean matches(CtReference element) {
                    return element.getSimpleName().equals(nameOfMethodThatMustBeInsideTheAssertion);
                }
            };
        }

        @Override
        public boolean matches(CtInvocation<?> element) {
            return (!element.getElements(namedElementFilter).isEmpty() ||
                    !element.getElements(referenceFilter).isEmpty()
            ) && AmplificationChecker.isAssert(element) && assertionName.equals(element.getExecutable().getSimpleName());
        }

        private final String assertionName;
        private final NamedElementFilter<CtNamedElement> namedElementFilter;

        private final TypeFilter<CtReference> referenceFilter;
    }

    @Test
    public void testOnFieldRead() throws Exception {

        /*
            Test that we can generate as expected variable in assertion field read such as DOUBLE.NEGATIVE_INFINITY
         */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithFieldRead");
        CtMethod test = Utils.findMethod(testClass, "test");

        List<CtMethod<?>> test_buildNewAssert = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test));
        CtMethod<?> amplifiedTestMethod = test_buildNewAssert.get(0);
        System.out.println(amplifiedTestMethod);
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("NEGATIVE_INFINITY", ASSERT_EQUALS)).size());
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getInfinity", ASSERT_EQUALS)).size());
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("NaN", ASSERT_EQUALS)).size());
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getNaN", ASSERT_EQUALS)).size());
//        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("MAX_VALUE", ASSERT_EQUALS)).size());
//        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getMax_VALUE", ASSERT_EQUALS)).size());
    }

    @Test
    public void testMultipleObservationsPoints() throws Exception {
        CtClass testClass = Utils.findClass("fr.inria.multipleobservations.TestClassToBeTest");
        CtMethod test = Utils.findMethod("fr.inria.multipleobservations.TestClassToBeTest", "test");
        List<CtMethod<?>> test_buildNewAssert = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test));
        CtMethod<?> amplifiedTestMethod = test_buildNewAssert.get(0);
        assertEquals(4, amplifiedTestMethod.getElements(AmplificationHelper.ASSERTIONS_FILTER).size());

        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getInt", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getInteger", ASSERT_EQUALS)).size());
    }



    @Test
    public void testBuildAssertOnSpecificCases() throws Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted");
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted", "test1");
        List<CtMethod<?>> test1_buildNewAssert = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test1));

        final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    int a = 0;" + AmplificationHelper.LINE_SEPARATOR +
                "    int b = 1;" + AmplificationHelper.LINE_SEPARATOR +
                "    int o_test1__3 = new java.util.Comparator<java.lang.Integer>() {" + AmplificationHelper.LINE_SEPARATOR +
                "        @java.lang.Override" + AmplificationHelper.LINE_SEPARATOR +
                "        public int compare(java.lang.Integer integer, java.lang.Integer t1) {" + AmplificationHelper.LINE_SEPARATOR +
                "            return integer - t1;" + AmplificationHelper.LINE_SEPARATOR +
                "        }" + AmplificationHelper.LINE_SEPARATOR +
                "    }.compare(a, b);" + AmplificationHelper.LINE_SEPARATOR +
                "    org.junit.Assert.assertEquals(-1, ((int) (o_test1__3)));" + AmplificationHelper.LINE_SEPARATOR +
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
				//TODO support generation of assertion on array
		 */
        CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        CtMethod<?> test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test1");
        CtMethod<?> amplifiedTestMethod = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test1)).get(0);
        assertEquals(37, amplifiedTestMethod.getBody().toString().split(AmplificationHelper.LINE_SEPARATOR).length); // 23 lines
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getBoolean", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getByte", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getShort", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getInt", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getLong", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getFloat", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getDouble", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getTrue", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getFalse", ASSERT_FALSE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getString", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getChar", ASSERT_EQUALS)).size());
        assertEquals(4, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("contains", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("isEmpty", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getNull", ASSERT_NULL)).size());
    }

    @Test
    public void testAssertsOnMaps() throws Exception {

        /*
            Test the generation of assertion on maps
         */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test3");
        List<CtMethod<?>> test1_buildNewAssert = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test1));
        final CtMethod<?> amplifiedTest = test1_buildNewAssert.get(0);
        final List<String> statementsAsString = amplifiedTest.getBody().getStatements().stream().map(CtStatement::toString).collect(Collectors.toList());
        assertEquals(16, amplifiedTest.getBody().getStatements().size());

        Arrays.stream(new String[]{
                "org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap) (classWithMap)).getEmptyMap().isEmpty())",
                "org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key1\"))",
                "org.junit.Assert.assertEquals(\"value1\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key1\"))",
                "org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key2\"))",
                "org.junit.Assert.assertEquals(\"value2\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key2\"))",
                "org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key3\"))",
                "org.junit.Assert.assertEquals(\"value3\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key3\"))"
        }).forEach(
                // each assertions should be present two time since we generate assertion just after the set up of the test,
                // and at the end of the test method if there is some method call that can potential change the state
                expected -> assertEquals(2, statementsAsString.stream().filter(expected::equals).count())
        );
    }

    @Test
    public void testMakeFailureTest() throws Exception {
        CtClass<?> testClass = Utils.findClass("fr.inria.filter.failing.FailingTest");
        final CtMethod testAssertionError = Utils.findMethod("fr.inria.filter.failing.FailingTest", "testAssertionError");
        final List<CtMethod<?>> generatedAssertion = assertGenerator.assertionAmplification(testClass, Collections.singletonList(testAssertionError));
        assertTrue(generatedAssertion.isEmpty());
    }

    @Test
    public void testMakeExpectedFailureTest() throws Exception {
        CtClass<?> testClass = Utils.findClass("fr.inria.filter.passing.PassingTest");
        final CtMethod testAssertionError = Utils.findMethod("fr.inria.filter.passing.PassingTest", "testNPEExpected");
        final List<CtMethod<?>> generatedAssertion = assertGenerator.assertionAmplification(testClass, Collections.singletonList(testAssertionError));
        assertFalse(generatedAssertion.isEmpty());
    }
}
