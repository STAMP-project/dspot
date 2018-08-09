package eu.stamp_project;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/14/17
 */
public class MainTest {

    @Before
    public void setUp() throws Exception {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("src/test/resources/test-projects/target"));
        } catch (Exception ignored) {

        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("src/test/resources/test-projects/target"));
        } catch (Exception ignored) {

        }
    }

    @Test
    public void testMainWithMixedListOfTestClassesAndTestMethods() throws Exception {

        /*
            Test that we can run the main with a mixed list of test classes and test methods.
                Since there are one list of test classes and one list of test methods, we do not differentiate in CLI which
                test method is in which test class.
            When such lists are provided, DSpot selects them correctly, i.e. it takes the correct test methods for each test classes
            Since we cannot associates each name of test methods to a specific test classes, if both test classes contain a test method
            that have the same name, both test methods will be amplified
         */

        Main.main(new String[]{
                "--clean",
                "--verbose",
                "--path-to-properties", "src/test/resources/sample/sample.properties",
                "--test-criterion", "TakeAllSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssert:fr.inria.sample.TestClassWithAssert",
                "--cases", "test1:test:anOldTest",
                "--no-minimize"
        });
        // an amplification happened, w/e it is
        CtClass<?> amplifiedTestClass = InputConfiguration.get().getFactory().Class().get("fr.inria.sample.TestClassWithAssert");
        assertNotNull(amplifiedTestClass);
        assertFalse(amplifiedTestClass.getMethods().isEmpty());
//        assertTrue(amplifiedTestClass.getMethods().stream().anyMatch(ctMethod -> ctMethod.getSimpleName().contains("anOldTest"))); // this test come from the super class
        amplifiedTestClass = InputConfiguration.get().getFactory().Class().get("fr.inria.sample.TestClassWithoutAssert");
        assertNotNull(amplifiedTestClass);
        assertFalse(amplifiedTestClass.getMethods().isEmpty());
    }

    @Test
    public void testMainWithEmptyTestMethods() throws Exception {
        Main.main(new String[]{
                "--clean",
                "--verbose",
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "TakeAllSelector",
                "--test", "example.TestSuiteExample",
                "--no-minimize"
        });
        final CtClass<?> amplifiedTestClass = InputConfiguration.get().getFactory().Class().get("example.TestSuiteExample");
        assertNotNull(amplifiedTestClass);
        assertFalse(amplifiedTestClass.getMethods().isEmpty());
    }

    @Test
    public void testOnProjectWithResources() throws Exception {
        Main.main(new String[]{
                "--verbose",
                "--path-to-properties", "src/test/resources/project-with-resources/project-with-resources.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--iteration", "1"
        });
    }

    @Test
    public void testExample() throws Exception {

        /*
            Test the --example option. It runs a specific predefined example of amplification.
                It also checks the auto imports output of DSpot.
         */

        Main.main(new String[]{"--verbose", "--example"});
        final File reportFile = new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage_report.txt");
        final File amplifiedTestClass = new File("target/trash/example/TestSuiteExample.java");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/trash/example.TestSuiteExample_jacoco_instr_coverage.json").exists());
        assertTrue(amplifiedTestClass.exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)) + AmplificationHelper.LINE_SEPARATOR;
            assertEquals(expectedReportExample, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(amplifiedTestClass))) {
            String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            System.out.println(content);
            System.out.println(content.startsWith(expectedAmplifiedTestClass));
//            assertTrue(content.startsWith(expectedAmplifiedTestClass));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //we  don't test the whole file, but only the begin of it. It is sufficient to detect the auto import.
    private static final String expectedAmplifiedTestClass = "package example;" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "import org.junit.Assert;" + AmplificationHelper.LINE_SEPARATOR +
            "import org.junit.Test;" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "public class TestSuiteExampleAmpl {" + AmplificationHelper.LINE_SEPARATOR +
            "    /* amplification of example.TestSuiteExample#test2 */" + AmplificationHelper.LINE_SEPARATOR +
            "    @Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
            "    public void test2_literalMutationString2() {" + AmplificationHelper.LINE_SEPARATOR +
            "        Example ex = new Example();" + AmplificationHelper.LINE_SEPARATOR +
            "        // AssertGenerator create local variable with return value of invocation" + AmplificationHelper.LINE_SEPARATOR +
            "        char o_test2_literalMutationString2__3 = ex.charAt(\"acd\", 3);" + AmplificationHelper.LINE_SEPARATOR +
            "        // AssertGenerator add assertion" + AmplificationHelper.LINE_SEPARATOR +
            "        Assert.assertEquals('d', ((char) (o_test2_literalMutationString2__3)));" + AmplificationHelper.LINE_SEPARATOR +
            "    }";

    private static final String expectedReportExample = AmplificationHelper.LINE_SEPARATOR +
            "======= REPORT =======" + AmplificationHelper.LINE_SEPARATOR +
            "Initial instruction coverage: 33 / 37" + AmplificationHelper.LINE_SEPARATOR +
            "89" + AmplificationHelper.DECIMAL_SEPARATOR + "19%" + AmplificationHelper.LINE_SEPARATOR +
            "Amplification results with 22 amplified tests." + AmplificationHelper.LINE_SEPARATOR +
            "Amplified instruction coverage: 37 / 37" + AmplificationHelper.LINE_SEPARATOR +
            "100" + AmplificationHelper.DECIMAL_SEPARATOR + "00%" + AmplificationHelper.LINE_SEPARATOR;

    @Test
    public void testOverrideExistingResults() throws Exception {

        /*
            Test that we can append result in different runs of DSpot, or not, according to the --clean (-q) flag
            Here, we run 4 time DSpot.
                    - 1 time with a lot of Amplifiers: result with a lof of amplified test
                    - then we if append result of run 2 and run 3, we obtain the same result than the 1
                    - the fourth is the same of the third time, but not appended to the result of the second
         */

        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + AmplificationHelper.PATH_SEPARATOR + "TestDataMutator" + AmplificationHelper.PATH_SEPARATOR + "MethodGeneratorAmplifier" + AmplificationHelper.PATH_SEPARATOR + "ReturnValueAmplifier",
                "--iteration", "1",
                "--randomSeed", "72",
                //"--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.TestSuiteExample",
                "--cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200"
        });
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("target/trash/example/TestSuiteExample.java");
        launcher.buildModel();
        final CtClass<?> testClass1 = launcher.getFactory().Class().get("example.TestSuiteExample");

        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + AmplificationHelper.PATH_SEPARATOR + "TestDataMutator",
                "--iteration", "1",
                "--randomSeed", "72",
                //"--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.TestSuiteExample",
                "--cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200",
                "--clean"
        });
        launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("target/trash/example/TestSuiteExample.java");
        launcher.buildModel();
        final CtClass<?> testClass2 = launcher.getFactory().Class().get("example.TestSuiteExample");


        // Assert that we do not have result from the first run
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodGeneratorAmplifier" + AmplificationHelper.PATH_SEPARATOR + "ReturnValueAmplifier",
                "--iteration", "1",
                "--randomSeed", "72",
                //"--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.TestSuiteExample",
                "--cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200",
        });
        launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("target/trash/example/TestSuiteExample.java");
        launcher.buildModel();
        final CtClass<?> testClass3 = launcher.getFactory().Class().get("example.TestSuiteExample");
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodGeneratorAmplifier" + AmplificationHelper.PATH_SEPARATOR + "ReturnValueAmplifier",
                "--iteration", "1",
                "--randomSeed", "72",
                //"--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "example.TestSuiteExample",
                "--cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200",
                "--clean"
        });
        launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("target/trash/example/TestSuiteExample.java");
        launcher.buildModel();
        final CtClass<?> testClass4 = launcher.getFactory().Class().get("example.TestSuiteExample");
        assertEquals(11, testClass1.getMethods().size());
        assertEquals(10, testClass2.getMethods().size());
        assertEquals(11, testClass3.getMethods().size());
        assertEquals(7, testClass4.getMethods().size());
    }
}
