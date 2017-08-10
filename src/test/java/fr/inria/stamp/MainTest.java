package fr.inria.stamp;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.utils.DSpotUtils;
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
public class MainTest {

    @Test
    public void testExample() throws Exception, InvalidSdkException {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{"--example"});
        final File reportFile = new File("target/trash/example.TestSuiteExample_branch_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_branch_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
//            assertEquals(expectedReportExample, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAll() throws Throwable {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.run(JSAPOptions.parse(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "BranchCoverageTestSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "all"
        }));
        final File reportFile = new File("target/trash/example.TestSuiteExample_branch_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_branch_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportAll, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    private static final String nl = System.getProperty("line.separator");

    private static final char DECIMAL_SEPARATOR = (((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator());

    private static final String expectedReportExample = nl +
            "======= REPORT =======" + nl +
            "Branch Coverage Selector:" + nl +
            "Initial coverage: 83" + DECIMAL_SEPARATOR + "33%" + nl +
            "There is 3 unique path in the original test suite" + nl +
            "The amplification results with 6 new tests" + nl +
            "The branch coverage obtained is: 100" + DECIMAL_SEPARATOR + "00%" + nl +
            "There is 3 new unique path" + nl + nl;

    private static final String expectedReportAll = nl +
            "======= REPORT =======" + nl +
            "Branch Coverage Selector:" + nl +
            "Initial coverage: 83" + DECIMAL_SEPARATOR + "33%" + nl +
            "There is 3 unique path in the original test suite" + nl +
            "The amplification results with 7 new tests" + nl +
            "The branch coverage obtained is: 100" + DECIMAL_SEPARATOR + "00%" + nl +
            "There is 4 new unique path" + nl + nl;

}
