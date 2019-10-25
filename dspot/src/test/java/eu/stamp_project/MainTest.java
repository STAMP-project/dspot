package eu.stamp_project;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

    @Ignore
    @Test
    public void testUsingCloverSelectorAndMultipleAmplificationDSpot() throws Exception {

        /*
            Test multiple amplification with CloverSelector
         */

        Main.main(new String[]{
                "--verbose",
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "CloverCoverageSelector",
                "--test", "all",
                "--no-minimize"
        });
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
                "--absolute-path-to-project-root", new File("src/test/resources/sample/").getAbsolutePath() + "/",
                "--test-criterion", "TakeAllSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssert,fr.inria.sample.TestClassWithAssert",
                "--test-cases", "test1,test,anOldTest"
        });
        // an amplification happened, w/e it is
        assertTrue(new File("target/dspot/output/fr/inria/sample/TestClassWithAssert.java").exists());
    }

    @Test
    public void testMainWithPitScoreSelector() throws Exception {

        /*
            Test the main procedure with the pit score selector
                - on two classes, the first result with some amplified test, the second does not have any test methods to be selected.
                it should not output the second class since there is no amplification.
                See https://github.com/STAMP-project/dspot/issues/601
         */

        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/sample/").getAbsolutePath() + "/",
                "--test-criterion", "PitMutantScoreSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssert,fr.inria.filter.failing.FailingTest",
                "--path-pit-result", "src/test/resources/sample/mutations.csv",
                "--gregor-mode",
                "--output-path", "target/trash",
        });

        assertTrue(new File("target/trash/fr.inria.sample.TestClassWithoutAssert_report.json").exists());
        assertFalse(new File("target/trash/fr/inria/filter/failing").exists());
        assertTrue(new File("target/trash/fr/inria/sample/").exists());
    }

    @Test
    public void testMainWithPitScoreSelectorWithMavenToExecuteTests() throws Exception {

        /*
            same as testMainWithPitScoreSelector but using the options --use-maven-to-exe-test enabled,
                i.e. using maven to execute the test
         */

        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/sample/").getAbsolutePath() + "/",
                "--test-criterion", "PitMutantScoreSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssert,fr.inria.filter.failing.FailingTest",
                "--path-pit-result", "src/test/resources/sample/mutations.csv",
                "--gregor-mode",
                "--output-path", "target/trash",
                "--use-maven-to-exe-test"
        });

        assertTrue(new File("target/trash/fr.inria.sample.TestClassWithoutAssert_report.json").exists());
        assertFalse(new File("target/trash/fr/inria/filter/failing").exists());
        assertTrue(new File("target/trash/fr/inria/sample/").exists());
    }

    @Test
    public void testMainWithPitScoreSelectorJUnit5() throws Exception {

        /*
            The same as testMainWithPitScoreSelector but with a JUnit5 test
         */

        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/sample/").getAbsolutePath() + "/",
                "--test-criterion", "PitMutantScoreSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssertJUnit5",
                "--path-pit-result", "src/test/resources/sample/mutations.csv",
                "--gregor-mode",
                "--output-path", "target/trash",
        });

        assertTrue(new File("target/trash/fr.inria.sample.TestClassWithoutAssertJUnit5_report.json").exists());
        assertFalse(new File("target/trash/fr/inria/filter/failing").exists());
        assertTrue(new File("target/trash/fr/inria/sample/").exists());
    }

    @Test
    public void testMainWithEmptyTestMethods() throws Exception {
        Main.main(new String[]{
                "--clean",
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--test-criterion", "TakeAllSelector",
                "--test", "example.TestSuiteExample"
        });
        assertTrue(new File("target/dspot/output/example/TestSuiteExample.java").exists());
    }

    @Test
    public void testOnParametrizedTestClass() throws Exception {

        /* run the main amplification process on a parametrized test method
         *   The configuration is the same than example, but in a parametrizesd test
         *
         */

        Main.main(new String[]{
                "--clean",
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--test-criterion", "JacocoCoverageSelector",
                "--iteration", "1",
                "--amplifiers", "FastLiteralAmplifier",
                "--test", "example.ParametrizedTestSuiteExample",
                "--input-ampl-distributor", "TextualDistanceInputAmplDistributor",
                "--test-cases", "test2"
        });
        assertTrue(new File("target/dspot/output/example/ParametrizedTestSuiteExample.java").exists());
    }

    @Test
    public void testOnProjectWithResources() throws Exception {
        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/project-with-resources/").getAbsolutePath() + "/",
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
        final File reportFile = new File("target/dspot/output/report.txt");
        final File amplifiedTestClass = new File("target/dspot/output/example/TestSuiteExample.java");
        assertTrue(reportFile.exists());
        assertTrue(new File("target/dspot/output/example.TestSuiteExample_report.json").exists());
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
            assertTrue(content.startsWith(expectedAmplifiedTestClass));
            assertTrue(new File("target/dspot/output/original/example/TestSuiteExample.java").exists());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //we  don't test the whole file, but only the begin of it. It is sufficient to detect the auto import.
    private static final String expectedAmplifiedTestClass = "package example;" + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "import org.junit.Assert;" + AmplificationHelper.LINE_SEPARATOR +
            "import org.junit.Test;" + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "public class TestSuiteExample {" + AmplificationHelper.LINE_SEPARATOR +
            "    @Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
            "    public void test3_literalMutationString50_failAssert0() throws Exception {" + AmplificationHelper.LINE_SEPARATOR +
            "        try {" + AmplificationHelper.LINE_SEPARATOR +
            "            Example ex = new Example();" + AmplificationHelper.LINE_SEPARATOR +
            "            String s = \"\";" + AmplificationHelper.LINE_SEPARATOR +
            "            ex.charAt(s, ((s.length()) - 1));" + AmplificationHelper.LINE_SEPARATOR +
            "            Assert.fail(\"test3_literalMutationString50 should have thrown StringIndexOutOfBoundsException\");" + AmplificationHelper.LINE_SEPARATOR +
            "        } catch (StringIndexOutOfBoundsException expected) {" + AmplificationHelper.LINE_SEPARATOR +
            "            Assert.assertEquals(\"String index out of range: 0\", expected.getMessage());" + AmplificationHelper.LINE_SEPARATOR +
            "        }" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR;

    private static final String expectedReportExample =
                    "Initial instruction coverage: 30 / 34" + AmplificationHelper.LINE_SEPARATOR +
                    "88" + AmplificationHelper.DECIMAL_SEPARATOR + "24%" + AmplificationHelper.LINE_SEPARATOR +
                    "Amplification results with 5 amplified tests." + AmplificationHelper.LINE_SEPARATOR +
                    "Amplified instruction coverage: 34 / 34" + AmplificationHelper.LINE_SEPARATOR +
                    "100" + AmplificationHelper.DECIMAL_SEPARATOR + "00%" + AmplificationHelper.LINE_SEPARATOR;

    @Test
    public void testOverrideExistingResults() throws Exception {

        /*
            Test that we can append result in different runs of DSpot, or not, according to the --clean (-q) flag
            Here, we run 4 time DSpot.
                    - 1 time with a lot of Amplifiers: result with a lot of amplified test
                    - then we append result of run 2 and run 3, we obtain the same result than the 1
                    - the fourth is the same of the third time, but not appended to the result of the second
         */

        // run 1: lot of amplifiers
        Main.main(new String[]{
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodDuplicationAmplifier,FastLiteralAmplifier,MethodAdderOnExistingObjectsAmplifier,ReturnValueAmplifier",
                "--iteration", "1",
                "--random-seed", "72",
                "--test", "example.TestSuiteExample",
                "--test-cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200",
                "--keep-original-test-methods"
        });
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("target/trash/example/TestSuiteExample.java");
        launcher.buildModel();
        final CtClass<?> testClass1 = launcher.getFactory().Class().get("example.TestSuiteExample");

        // run 2: some amplifiers
        Main.main(new String[]{
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodDuplicationAmplifier,FastLiteralAmplifier",
                "--iteration", "1",
                "--random-seed", "72",
                "--test", "example.TestSuiteExample",
                "--test-cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200",
                "--keep-original-test-methods",
                "--clean"
        });
        launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("target/trash/example/TestSuiteExample.java");
        launcher.buildModel();
        final CtClass<?> testClass2 = launcher.getFactory().Class().get("example.TestSuiteExample");


        // Assert that we do not have result from the first run
        // run 3: some amplifiers
        // run 2 + 3 = run 1
        Main.main(new String[]{
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdderOnExistingObjectsAmplifier,ReturnValueAmplifier",
                "--iteration", "1",
                "--random-seed", "72",
                "--test", "example.TestSuiteExample",
                "--test-cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200",
                "--keep-original-test-methods"
        });
        launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("target/trash/example/TestSuiteExample.java");
        launcher.buildModel();
        final CtClass<?> testClass3 = launcher.getFactory().Class().get("example.TestSuiteExample");

        //run 4: equals to run 1 minus run 2 = run 3
        Main.main(new String[]{
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodDuplicationAmplifier,ReturnValueAmplifier",
                "--iteration", "1",
                "--random-seed", "72",
                "--test", "example.TestSuiteExample",
                "--test-cases", "test2",
                "--output-path", "target/trash",
                "--max-test-amplified", "200",
                "--keep-original-test-methods",
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
