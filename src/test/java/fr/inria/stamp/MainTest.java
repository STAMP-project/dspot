package fr.inria.stamp;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.MavenAbstractTest;
import org.apache.maven.Maven;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/14/17
 */
public class MainTest extends MavenAbstractTest {

    @Test
    public void testAll() throws Exception {
        try {
            Main.main(new String[]{
                    "--path-to-propeties", "src/test/resources/test-projects/test-projects.properties",
                    "--test-criterion", "BranchCoverageTestSelector",
                    "--amplifiers", "MethodAdd:TestDataMutator:StatementAdderOnAssert",
                    "--iteration", "1",
            });
        } catch (InvalidSdkException e) {
            e.printStackTrace();
        }
        final File reportFile = new File("dspot-out/example.TestSuiteExample_branch_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("dspot-out/example.TestSuiteExample_branch_coverage.json").exists());
        assertTrue(new File("dspot-out/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportAll, content);
        } catch (IOException e ){
            throw new RuntimeException(e);
        }
    }

    private static final String nl = System.getProperty("line.separator");

    private static final String expectedReportAll = nl +
            "======= REPORT =======" + nl +
            "Branch Coverage Selector:" + nl +
            "Initial coverage: 83.33%" + nl +
            "There is 3 unique path in the original test suite" + nl +
            "The amplification results with 6 new tests" + nl +
            "The branch coverage obtained is: 100.00%" + nl +
            "There is 3 new unique path" + nl + nl;

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
