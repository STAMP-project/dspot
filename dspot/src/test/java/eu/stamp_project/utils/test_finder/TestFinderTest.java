package eu.stamp_project.utils.test_finder;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.test_framework.TestFramework;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/09/19
 */
public class TestFinderTest extends AbstractTestOnSample {

    @Override
    public String getPathToProjectRoot() {
        return new File("src/test/resources/test-projects/").getAbsolutePath() + "/";
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        testFinder =  new TestFinder(Collections.emptyList(), Collections.emptyList());
    }

    private TestFinder testFinder;

    @Test
    public void testFindClasses2Classes() {
        /*
            Can match several test classes
         */

        final List<CtType<?>> testClasses = testFinder.findTestClasses(
                Arrays.asList(
                        "example.TestSuiteExample",
                        "example.TestSuiteExample2"
                )
        );
        assertEquals(2, testClasses.size());
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
    }

    @Test
    public void testOneClassTwoMethods() {
         /*
            Can match specific test method in a test class
         */

        final List<CtType<?>> testClasses = testFinder.findTestClasses(Collections.singletonList("example.TestSuiteExample"));
        final List<CtMethod<?>> testMethods = testFinder.findTestMethods(
                testClasses.get(0),
                Arrays.asList("test2", "test3")
        );
        assertEquals(1, testClasses.size());
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertEquals(2, testMethods.size());
        assertTrue(testMethods.stream().map(CtMethod::getSimpleName).anyMatch("test2"::equals));
        assertTrue(testMethods.stream().map(CtMethod::getSimpleName).anyMatch("test3"::equals));
    }


    @Test
    public void testRegexOnWholePackage() throws Throwable {

        /*
            Can match test classes using a regex
                The test class TestResources is not selected since it does not contain any test method.
         */

        final List<CtType<?>> testClasses = testFinder.findTestClasses(Collections.singletonList("example.*"));
        assertEquals(3, testClasses.size());
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.ParametrizedTestSuiteExample"::equals));
    }

    @Test
    public void testUsingRegex() throws Throwable {

        /*
            Can match test classes using a regex
         */

        final List<CtType<?>> testClasses = testFinder.findTestClasses(Collections.singletonList("example.TestSuiteExample*"));
        assertEquals(2, testClasses.size());
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
    }

    @Test
    public void testAll() throws Exception {
        /*
            Can match all test classes
         */

        final List<CtType<?>> testClasses = testFinder.findTestClasses(Collections.emptyList());
        assertEquals(3, testClasses.size());
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample"::equals));
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.TestSuiteExample2"::equals));
        assertTrue(testClasses.stream().map(CtType::getQualifiedName).anyMatch("example.ParametrizedTestSuiteExample"::equals));
    }

}
