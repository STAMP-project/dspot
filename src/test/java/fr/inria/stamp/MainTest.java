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

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    private static final String nl = System.getProperty("line.separator");

    private static final char DECIMAL_SEPARATOR = (((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator());


    @Test
    public void testExample() throws Exception, InvalidSdkException {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{"--verbose", "--example"});
        final File reportFile = new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportExample, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTwoClasses() throws Exception, InvalidSdkException {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.TestSuiteExample:example.TestSuiteExample2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200"
        });
        final File reportFile = new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportAll, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String expectedReportTwoClass = nl +
            "======= REPORT =======" + nl +
            "Branch Coverage Selector:" + nl +
            "Initial coverage: 83.33%" + nl +
            "There is 3 unique path in the original test suite" + nl +
            "The amplification results with 7 new tests" + nl +
            "The branch coverage obtained is: 100.00%" + nl +
            "There is 4 new unique path" + nl + nl;

    @Test
    public void testOneClassOneMethod() throws Throwable {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.TestSuiteExample",
                "--cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200"
        });
        final File reportFile = new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportOneClassOneMethod, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String expectedReportOneClassOneMethod = "" + nl +
            "======= REPORT =======" + nl +
            "Initial instruction coverage: 33 / 37" + nl +
            "89.19%" + nl +
            "Amplification results with 6 amplified tests." + nl +
            "Amplified instruction coverage: 37 / 37" + nl +
            "100.00%" + nl;

    @Test
    public void testRegexOnWholePackage() throws Throwable {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.*",
                "--output-path", "target/trash",
                "--max-test-amplified", "200"
        });
        final File reportFile = new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage_report.txt");
        assertTrue(reportFile.exists());
        final File reportFile2 = new File("target/trash/example.TestSuiteExample2_jacoco_instr_coverage_report.txt");
        assertTrue(reportFile2.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage.json").exists());
        assertTrue(new File("target/trash/example.TestSuiteExample2_jacoco_instr_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        assertTrue(new File("target/trash/example/TestSuiteExample2Ampl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportAll, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUsingRegex() throws Throwable {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.TestSuite*",
                "--output-path", "target/trash",
                "--max-test-amplified", "200"
        });
        final File reportFile = new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportAll, content);
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
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "all",
                "--output-path", "target/trash",
                "--max-test-amplified", "200"
        });
        final File reportFile = new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage_report.txt");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage.json").exists());
        assertTrue(new File("target/trash/example/TestSuiteExampleAmpl.java").exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().reduce("", (acc, line) -> acc + line + nl);
            assertEquals(expectedReportAll, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String expectedReportExample = nl +
            "======= REPORT =======" + nl +
            "Initial instruction coverage: 33 / 37" + nl +
            "89.19%" + nl +
            "Amplification results with 24 amplified tests." + nl +
            "Amplified instruction coverage: 37 / 37" + nl +
            "100.00%" + nl;

    private static final String expectedReportAll = nl +
            "======= REPORT =======" + nl +
            "Initial instruction coverage: 33 / 37" + nl +
            "89.19%" + nl +
            "Amplification results with 31 amplified tests." + nl +
            "Amplified instruction coverage: 37 / 37" + nl +
            "100.00%" + nl;

}
