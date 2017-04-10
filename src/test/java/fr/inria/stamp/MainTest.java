package fr.inria.stamp;

import fr.inria.diversify.dspot.MavenAbstractTest;
import fr.inria.diversify.util.FileUtils;
import org.junit.Test;

import java.io.*;
import java.text.DecimalFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/14/17
 */
public class MainTest extends MavenAbstractTest {

    @Test
    public void testAll() throws Throwable {
        FileUtils.deleteDirectory(new File("dspot-out"));
        Main.run(JSAPOptions.parse(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "BranchCoverageTestSelector",
                "--amplifiers", "MethodAdd:TestDataMutator:StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72"
        }));
        final File reportFile = new File("dspot-out/example.TestSuiteExample_branch_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("dspot-out/example.TestSuiteExample_branch_coverage.json").exists());
        assertTrue(new File("dspot-out/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportAll, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String nl = System.getProperty("line.separator");

    private static final char DECIMAL_SEPARATOR = (((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator());

    private static final String expectedReportAll = nl +
            "======= REPORT =======" + nl +
            "Branch Coverage Selector:" + nl +
            "Initial coverage: 83" + DECIMAL_SEPARATOR + "33%" + nl +
            "There is 3 unique path in the original test suite" + nl +
            "The amplification results with 6 new tests" + nl +
            "The branch coverage obtained is: 100" + DECIMAL_SEPARATOR + "00%" + nl +
            "There is 3 new unique path" + nl + nl;

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
