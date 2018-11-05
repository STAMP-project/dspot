package eu.stamp_project.dspot;

import eu.stamp_project.Utils;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.Main;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/03/18
 */
public class DSpotSelectionTest {

    /*
        In this test class, we test the selection of the test to be amplified
     */

    private static DSpot dspotUnderTest;

    private static class MockedDSpot extends DSpot {
        public MockedDSpot(int numberOfIterations,
                           TestSelector testSelector) throws Exception {
            super(numberOfIterations, testSelector);
        }

        @Override
        protected CtType _amplify(CtType test, List<CtMethod<?>> methods) {
            methodsToBeAmplified.addAll(methods);
            typesToBeAmplified.add(test);
            return test;
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Utils.reset();
        final InputConfiguration inputConfiguration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        Main.createOutputDirectories(inputConfiguration);
        dspotUnderTest = new MockedDSpot(
                1,
                new JacocoCoverageSelector()
        );
    }

    private static List<CtMethod> methodsToBeAmplified = new ArrayList<>();

    private static List<CtType> typesToBeAmplified = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        methodsToBeAmplified.clear();
        typesToBeAmplified.clear();
    }

    @Test
    public void testOnTwoClass2() throws Exception {

        /*
            Can match several test classes
         */

        dspotUnderTest.amplifyTestClasses(
                Arrays.asList(
                        "example.TestSuiteExample",
                        "example.TestSuiteExample2"
                )
        );
        assertEquals(2, typesToBeAmplified.size());
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
    }

    @Test
    public void testOneClassTwoMethods() throws Throwable {

        /*
            Can match specific test method in a test class
         */

        dspotUnderTest.amplifyTestClassTestMethods(
                "example.TestSuiteExample",
                Arrays.asList("test2", "test3")
        );
        assertEquals(1, typesToBeAmplified.size());
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertEquals(2, methodsToBeAmplified.size());
        assertTrue(methodsToBeAmplified.stream().map(CtMethod::getSimpleName).anyMatch("test2"::equals));
        assertTrue(methodsToBeAmplified.stream().map(CtMethod::getSimpleName).anyMatch("test3"::equals));
    }

    @Test
    public void testRegexOnWholePackage() throws Throwable {

        /*
            Can match test classes using a regex
                The test class TestResources is not selected since it does not contain any test method.
         */

        dspotUnderTest.amplifyTestClass("example.*");
        assertEquals(2, typesToBeAmplified.size());
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
    }

    @Test
    public void testUsingRegex() throws Throwable {

        /*
            Can match test classes using a regex
         */

        dspotUnderTest.amplifyTestClass("example.TestSuiteExample*");
        assertEquals(2, typesToBeAmplified.size());
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
    }

    @Test
    public void testAll() throws Exception {
        /*
            Can match all test classes
         */

        dspotUnderTest.amplifyAllTests();
        assertEquals(2, typesToBeAmplified.size());
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(typesToBeAmplified.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
    }
}
