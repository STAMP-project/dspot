package eu.stamp_project.diff_test_selection.clover;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;

public class CloverReaderTest {

    @Test
    public void test() {

        /*
         *  Read a clover report and return the coverage
         */

        final Map<String, Map<String, Map<String, List<Integer>>>> coverage = new CloverReader().read("src/test/resources/clover_report");
        assertFalse(coverage.isEmpty());
    }
}
