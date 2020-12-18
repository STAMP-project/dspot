package eu.stamp_project.diff_test_selection.clover;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class CloverExectorTest {


    @Test
    public void test() {

        /*
        * Test the CloverExecutor : it runs openclover on the test suite
        * The oracle is that the folder /target/clover exists
        */
        final String pathToRootOfProject = "./src/test/resources/tavern";
        new CloverExecutor().instrumentAndRunTest(pathToRootOfProject);
        assertTrue(new File(pathToRootOfProject + "/clover_report/clover").exists());
    }
}
