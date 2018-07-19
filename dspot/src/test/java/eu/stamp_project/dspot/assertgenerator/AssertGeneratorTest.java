package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
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
    public class AssertionFilterName extends TypeFilter<CtInvocation<?>> {

        public AssertionFilterName(String nameOfMethodThatMustBeInsideTheAssertion, String assertionName) {
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

    ;

    @Test
    public void testMultipleObservationsPoints() throws Exception {
        CtClass testClass = Utils.findClass("fr.inria.multipleobservations.TestClassToBeTest");
        CtMethod test = Utils.findMethod("fr.inria.multipleobservations.TestClassToBeTest", "test");
        List<CtMethod<?>> test_buildNewAssert = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test));
        CtMethod<?> amplifiedTestMethod = test_buildNewAssert.get(0);
        assertEquals(4, amplifiedTestMethod.getElements(AmplificationHelper.ASSERTIONS_FILTER).size());

        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getInt", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getInteger", ASSERT_EQUALS)).size());
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
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getBoolean", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getByte", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getShort", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getInt", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getLong", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getFloat", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getDouble", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getTrue", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getFalse", ASSERT_FALSE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getString", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getChar", ASSERT_EQUALS)).size());
        assertEquals(4, amplifiedTestMethod.getElements(new AssertionFilterName("contains", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("isEmpty", ASSERT_TRUE)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterName("getNull", ASSERT_NULL)).size());
    }

    @Test
    public void testAssertsOnMaps() throws Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test3");
        List<CtMethod<?>> test1_buildNewAssert = assertGenerator.assertionAmplification(testClass, Collections.singletonList(test1));
        assertEquals(expectedBodyWithMap, test1_buildNewAssert.get(0).getBody().toString());
    }

    private static final String expectedBodyWithMap = "{" + AmplificationHelper.LINE_SEPARATOR +
            "    final fr.inria.sample.ClassWithMap classWithMap = new fr.inria.sample.ClassWithMap();" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap) (classWithMap)).getEmptyMap().isEmpty());" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key1\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertEquals(\"value1\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key1\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key2\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertEquals(\"value2\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key2\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key3\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertEquals(\"value3\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key3\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    classWithMap.getFullMap();" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap) (classWithMap)).getEmptyMap().isEmpty());" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key1\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertEquals(\"value1\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key1\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key2\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertEquals(\"value2\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key2\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertTrue(((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().containsKey(\"key3\"));" + AmplificationHelper.LINE_SEPARATOR +
            "    org.junit.Assert.assertEquals(\"value3\", ((fr.inria.sample.ClassWithMap)classWithMap).getFullMap().get(\"key3\"));" + AmplificationHelper.LINE_SEPARATOR +
            "}";

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
        assertTrue(generatedAssertion.isEmpty());
    }
}
