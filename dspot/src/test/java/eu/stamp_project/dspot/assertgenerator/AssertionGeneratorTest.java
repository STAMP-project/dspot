package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.assertgenerator.components.AssertionRemover;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/4/17
 */
public class AssertionGeneratorTest extends AbstractTest {

    public static final String THE_METHOD_IS_EMPTY_IS_UNDEFINED_FOR_THE_TYPE = "The method isEmpty() is undefined for the type ";
    private AssertionGenerator assertionGenerator;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.assertionGenerator = new AssertionGenerator(Utils.getInputConfiguration(), Utils.getCompiler());
    }

    @After
    public void tearDown() throws Exception {
        Utils.getInputConfiguration().setWithComment(false);
        Utils.getInputConfiguration().setTimeOutInMs(10000);
    }

    @Test
    public void testOnTestWithAssertedValues() {

        /*
            If there is any asserted values, DSpot should regenerate an assertion on them
         */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithAssert");
        final CtMethod<?> test1 = (CtMethod<?>) testClass.getMethodsByName("testWithNewSomethingWithoutLocalVariables").get(0);
        final List<CtMethod<?>> assertionAmplifications =
                this.assertionGenerator.assertionAmplification(testClass, Collections.singletonList(test1));
        System.out.println(assertionAmplifications);
    }

    @Test
    public void testCreateLogOnClassObject() throws Exception {
        final CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        final CtMethod<?> testOnClass = Utils.findMethod(testClass, "testOnClass");
        final AssertionRemover assertionRemover = new AssertionRemover();
        final CtMethod<?> testWithoutAssertions = assertionRemover.removeAssertion(testOnClass);
        System.out.println(
                assertionGenerator.assertionAmplification(testClass, Collections.singletonList(testWithoutAssertions))
        );
    }

    @Test
    public void testOnInfiniteLoop() throws Exception {
        Utils.getInputConfiguration().setTimeOutInMs(1000);
        final CtClass testClass = Utils.findClass("fr.inria.infinite.LoopTest");
        CtMethod test = Utils.findMethod("fr.inria.infinite.LoopTest", "testLoop");
        List<CtMethod<?>> test_buildNewAssert = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(test));
        assertTrue(test_buildNewAssert.isEmpty());
    }

    private static final String ASSERT_EQUALS = "assertEquals";
    private static final String ASSERT_TRUE = "assertTrue";
    private static final String ASSERT_FALSE = "assertFalse";
    private static final String ASSERT_NULL = "assertNull";

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
            ) && TestFramework.get().isAssert(element) && assertionName.equals(element.getExecutable().getSimpleName());
        }

        private final String assertionName;
        private final NamedElementFilter<CtNamedElement> namedElementFilter;

        private final TypeFilter<CtReference> referenceFilter;
    }

    @Test
    public void testOnFieldRead() {

        /*
            Test that we can generate as expected variable in assertion field read such as DOUBLE.NEGATIVE_INFINITY
         */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithFieldRead");
        CtMethod test = Utils.findMethod(testClass, "test");

        List<CtMethod<?>> test_buildNewAssert = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(test));
        CtMethod<?> amplifiedTestMethod = test_buildNewAssert.get(0);
        System.out.println(amplifiedTestMethod);
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("NEGATIVE_INFINITY", ASSERT_EQUALS)).size());
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getInfinity", ASSERT_EQUALS)).size());
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("NaN", ASSERT_EQUALS)).size());
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getNaN", ASSERT_EQUALS)).size());
        assertEquals(1, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getMax_VALUE", ASSERT_EQUALS)).size());
    }

    @Test
    public void testMultipleObservationsPoints() {
        CtClass testClass = Utils.findClass("fr.inria.multipleobservations.TestClassToBeTest");
        CtMethod test = Utils.findMethod("fr.inria.multipleobservations.TestClassToBeTest", "test");
        List<CtMethod<?>> test_buildNewAssert = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(test));
        CtMethod<?> amplifiedTestMethod = test_buildNewAssert.get(0);
        assertEquals(4, amplifiedTestMethod.getElements(TestFramework.ASSERTIONS_FILTER).size());

        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getInt", ASSERT_EQUALS)).size());
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getInteger", ASSERT_EQUALS)).size());
    }


    @Test
    public void testBuildAssertOnSpecificCases() {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted");
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted", "test1");
        List<CtMethod<?>> test1_buildNewAssert = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(test1));

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

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionGeneratorTest.class);

    @Test
    public void testBuildNewAssert() {
        LOGGER.info("Running testBuildNewAssert");

        /*
			DSpot is able to generate multiple assertion using getter inside the targeted class
				- Boolean (assertTrue / assertFalse)
				- primitive type and String (assertEquals)
				- null value (assertNull)
				- Collection: with elements (assertTrue(contains())) and empty (assertTrue(isEmpty()))
				- Iterable
				//TODO support generation of assertion on array
		 */
        CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        CtMethod<?> test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test1");
        final List<CtMethod<?>> ctMethods = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(test1));
        if (ctMethods.isEmpty()) {
            fail("the assertion amplification should have result with at least one test.");
        }
        CtMethod<?> amplifiedTestMethod = ctMethods.get(0);
        LOGGER.info("{}", amplifiedTestMethod.toString());
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
        assertEquals(2, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getNull", ASSERT_NULL)).size());
        assertEquals(4, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("isEmpty", ASSERT_TRUE)).size());
        // must not match
        assertEquals(0, amplifiedTestMethod.getElements(new AssertionFilterNameOnInvocation("getEmptyMyIterable", ASSERT_TRUE)).size());
        // check that the amplified test method can be compiled
        final CtClass<?> clone = testClass.clone();
        clone.addMethod(amplifiedTestMethod);
        DSpotUtils.printCtTypeToGivenDirectory(clone, Utils.getCompiler().getSourceOutputDirectory());
        final List<CategorizedProblem> categorizedProblems = Utils.getCompiler().compileAndReturnProblems(InputConfiguration.get().getClasspathClassesProject());
        assertTrue(
                categorizedProblems.stream().filter(categorizedProblem ->
                                categorizedProblem.getMessage().startsWith(THE_METHOD_IS_EMPTY_IS_UNDEFINED_FOR_THE_TYPE))
                        .map(CategorizedProblem::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)),
                categorizedProblems.stream()
                        .noneMatch(categorizedProblem ->
                                categorizedProblem.getMessage().startsWith(THE_METHOD_IS_EMPTY_IS_UNDEFINED_FOR_THE_TYPE)
                        )
        );
    }

    @Test
    public void testAssertsOnMaps() throws Exception {
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithoutAssert", "test3");
        List<CtMethod<?>> test1_buildNewAssert = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(test1));
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
    public void testCannotGenerateTestOnEmptyBodyTest() throws Exception {

        /*
            Test that when the test has an empty body.
         */

        CtClass<?> testClass = Utils.findClass("fr.inria.filter.failing.FailingTest");
        final CtMethod testAssertionError = Utils.findMethod("fr.inria.filter.failing.FailingTest", "testAssertionError");
        final List<CtMethod<?>> generatedAssertion = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(testAssertionError));
        assertTrue(generatedAssertion.isEmpty());
    }

    @Test
    public void testMakeExpectedFailureTest() throws Exception {

        /*
            Test that DSpot generate a try catch fail block when a Exception is thrown
         */

        CtClass<?> testClass = Utils.findClass("fr.inria.filter.passing.PassingTest");
        final CtMethod testAssertionError = Utils.findMethod("fr.inria.filter.passing.PassingTest", "testNPEExpected");
        final List<CtMethod<?>> generatedAssertion = assertionGenerator.assertionAmplification(testClass, Collections.singletonList(testAssertionError));
        System.out.println(generatedAssertion);
        assertFalse(generatedAssertion.isEmpty());
    }
}
