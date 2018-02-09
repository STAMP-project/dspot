package fr.inria.stamp.test.runner;

import fr.inria.stamp.test.listener.TestListener;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/07/17.
 */
@Deprecated
public class MockitoTestRunnerTest {

    @Test
    public void testRunTestClass() throws Exception {
        TestRunner runner = new DefaultTestRunner(
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar"
                                + System.getProperty("path.separator") +
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar");
        TestListener results = runner.run("info.sanaulla.dal.BookDALTest");
        assertEquals(5, results.getRunningTests().size());
        assertEquals(1, results.getFailingTests().size());
        assertEquals(4, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testRunTestMethod() throws Exception {
        TestRunner runner = new DefaultTestRunner(
                new String[]{
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("info.sanaulla.dal.BookDALTest", "testGetAllBooks");
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
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("info.sanaulla.dal.BookDALTest",
                Arrays.asList(new String[]{"testGetAllBooks", "testGetAllBooksFailing"}));
        assertEquals(2, results.getRunningTests().size());
        assertEquals(1, results.getFailingTests().size());
        assertEquals(1, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }
}
