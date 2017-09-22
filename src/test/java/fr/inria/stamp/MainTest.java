package fr.inria.stamp;

import fr.inria.diversify.sosiefier.runner.InputConfiguration;
import fr.inria.diversify.utils.DSpotUtils;
import org.apache.commons.io.FileUtils;
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
    public void testExample() throws Exception {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
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

    private static final String expectedReportExample = nl +
            "======= REPORT =======" + nl  +
            "Initial instruction coverage: 33 / 37" + nl  +
            "89" + DECIMAL_SEPARATOR + "19%" + nl  +
            "Amplification results with 35 amplified tests." + nl  +
            "Amplified instruction coverage: 37 / 37" + nl  +
            "100" + DECIMAL_SEPARATOR + "00%" + nl ;

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
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" ,
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

    private static final String expectedReportOneClassOneMethod = nl +
            "======= REPORT =======" + nl +
            "Initial instruction coverage: 33 / 37" + nl +
            "89" + DECIMAL_SEPARATOR + "19%" + nl +
            "Amplification results with 5 amplified tests." + nl +
            "Amplified instruction coverage: 37 / 37" + nl +
            "100" + DECIMAL_SEPARATOR + "00%" + nl;

    @Test
    public void testRegex() throws Throwable {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" ,
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.*",
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
    public void testOneClass() throws Throwable {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("tmpDir"));
        } catch (Exception ignored) {

        }
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" ,
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
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" ,
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

    private static final String expectedReportAll = nl +
            "======= REPORT =======" + nl +
            "Initial instruction coverage: 33 / 37" + nl +
            "89" + DECIMAL_SEPARATOR + "19%" + nl +
            "Amplification results with 24 amplified tests." + nl +
            "Amplified instruction coverage: 37 / 37" + nl +
            "100" + DECIMAL_SEPARATOR + "00%" + nl;

}
