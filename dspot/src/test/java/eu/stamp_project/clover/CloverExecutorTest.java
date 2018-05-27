package eu.stamp_project.clover;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/12/17
 */
public class CloverExecutorTest extends AbstractTest {

    /*
        The CloverExecutor should return a map, containing the contribution of each executed test cases, i.e.,
            each lines of the source code executed by each test cases of the given test class
            This is a map of the name of the test case to a map of Source classes name to the number of line executed:
            Map<String, Map<String, List<Integer>> :
            {testMethod1 -> [ {Class1 -> 1,2,3}, {Class2 -> 5,7,8}, ...], testMethod2 -> ... }
     */

    @Override
    public String getPathToPropertiesFile() {

        return "src/test/resources/test-projects/test-projects.properties";
    }

    @Test
    public void testExecuteAll() throws Exception {
        /*
            Test the method executeAll, on all test classes, of CloverExecutor:

         */

        final Map<String, Map<String, List<Integer>>> lineCoveragePerTestMethod =
                CloverExecutor.executeAll(Utils.getInputConfiguration(), Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + "/src/");
        final Map<String, List<Integer>> test4 = lineCoveragePerTestMethod.get("test4");
        assertTrue(test4.containsKey("example.Example"));
        assertEquals(1, test4.keySet().size());
        assertEquals("[12, 15, 18, 22, 23, 24]", test4.get("example.Example").toString());

        final Map<String, List<Integer>> test8 = lineCoveragePerTestMethod.get("test8");
        assertTrue(test8.containsKey("example.Example"));
        assertEquals(1, test8.keySet().size());
        assertEquals("[12, 15, 16, 22, 23, 24]", test8.get("example.Example").toString());
    }

    @Test
    public void testExecute() throws Exception {

        /*
            Test the method execute, on one single test class, of CloverExecutor:
                We test two coverages: test4 and test8. These tests cover 1 class, but different lines.
         */

        final Map<String, Map<String, List<Integer>>> lineCoveragePerTestMethod =
                CloverExecutor.execute(Utils.getInputConfiguration(), Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + "/src/", "example.TestSuiteExample");
        final Map<String, List<Integer>> test4 = lineCoveragePerTestMethod.get("test4");
        assertTrue(test4.containsKey("example.Example"));
        assertEquals(1, test4.keySet().size());
        assertEquals("[12, 15, 18, 22, 23, 24]", test4.get("example.Example").toString());

        final Map<String, List<Integer>> test8 = lineCoveragePerTestMethod.get("test8");
        assertTrue(test8.containsKey("example.Example"));
        assertEquals(1, test8.keySet().size());
        assertEquals("[12, 15, 16, 22, 23, 24]", test8.get("example.Example").toString());
    }
}
