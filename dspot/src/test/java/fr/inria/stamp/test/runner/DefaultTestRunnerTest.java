package fr.inria.stamp.test.runner;

import fr.inria.stamp.test.listener.TestListener;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
@Deprecated
public class DefaultTestRunnerTest {

    @Test
    public void testRunTestClass() throws Exception {
        TestRunner runner = new DefaultTestRunner(
                        "src/test/resources/example-0.0.1-SNAPSHOT.jar"
                                + System.getProperty("path.separator") +
                        "src/test/resources/example-0.0.1-SNAPSHOT-tests.jar");
        TestListener results = runner.run("example.TestSuiteExample");
        assertEquals(8, results.getRunningTests().size());
        assertEquals(1, results.getFailingTests().size());
        assertEquals(6, results.getPassingTests().size());
        assertEquals(1, results.getAssumptionFailingTests().size());
        assertEquals(1, results.getIgnoredTests().size());
    }

    @Test
    public void testRunTestMethod() throws Exception {
        TestRunner runner = new DefaultTestRunner(
                new String[]{
                        "src/test/resources/example-0.0.1-SNAPSHOT.jar",
                        "src/test/resources/example-0.0.1-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("example.TestSuiteExample", "test3");
        assertEquals(1, results.getRunningTests().size());
        assertEquals(0, results.getFailingTests().size());
        assertEquals(1, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testRunTestMethods() throws Exception {
        TestRunner runner = new DefaultTestRunner(
                new String[]{
                        "src/test/resources/example-0.0.1-SNAPSHOT.jar",
                        "src/test/resources/example-0.0.1-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("example.TestSuiteExample",
                Arrays.asList(new String[]{"test3", "test1"}));
        assertEquals(2, results.getRunningTests().size());
        assertEquals(1, results.getFailingTests().size());
        assertEquals(1, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testParameterizedTest() throws Exception {

        /*
            test the stamp.fr.inria.runner on parametized test:
                - Injection of parameters with constructors
                - Injection of parameters with fields
         */

        TestRunner runner = new DefaultTestRunner(
                new String[]{
                        "src/test/resources/parametized-0.0.1-SNAPSHOT.jar",
                        "src/test/resources/parametized-0.0.1-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("example.ConstructorParameterizedTest");
        assertEquals(10, results.getRunningTests().size());
        assertEquals(0, results.getFailingTests().size());
        assertEquals(10, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
        results = runner.run("example.ParameterizedTest");
        assertEquals(5, results.getRunningTests().size());
        assertEquals(0, results.getFailingTests().size());
        assertEquals(5, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testParameterizedTestSpecificMethod() throws Exception {
        TestRunner runner = new DefaultTestRunner(
                new String[]{
                        "src/test/resources/parametized-0.0.1-SNAPSHOT.jar",
                        "src/test/resources/parametized-0.0.1-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("example.ConstructorParameterizedTest", "test_addTwoNumber");
        assertEquals(5, results.getRunningTests().size());
        assertEquals(0, results.getFailingTests().size());
        assertEquals(5, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }
}
