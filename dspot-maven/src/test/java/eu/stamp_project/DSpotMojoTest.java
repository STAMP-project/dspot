package eu.stamp_project;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.codehaus.plexus.PlexusTestCase.getBasedir;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/18
 * found on https://maven.apache.org/plugin-developers/plugin-testing.html
 */
public class DSpotMojoTest {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    /*
        The mojo under test
     */
    private DSpotMojo mojoUnderTest;

    @Before
    public void setUp() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/test-projects/");
        mojoUnderTest = (DSpotMojo) mojoRule.lookupConfiguredMojo(testPom , "amplify-unit-tests");
        mojoUnderTest.setVerbose(true);
    }

    @Test
    public void testDefaultConfiguration() throws Exception {

        /*
            We just need to specify a path to properties to execute dspot, nothing more.
            WEAK ORACLE: we just execute the mojo, if everything is fine, it is okay
         */
        mojoUnderTest.setPathToProperties("src/test/resources/test-projects/test-projects.properties");

        mojoUnderTest.setPathPitResult("src/test/resources/test-projects/mutations.csv");
        mojoUnderTest.setTestCriterion("PitMutantScoreSelector");
        mojoUnderTest.setOutputPath("target/dspot-output");

        mojoUnderTest.execute();
    }

    @Test
    public void testTargetingSpecificTestMethods() throws Exception {

        /*
            In this configuration, we execute dspot on a specific test methods
            In order to obtain result, we set up also amplifiers and selector
            We verify the result, i.e. the report txt

            In this configuration, we retrieve from the pom, the information we need, e.g. source folders etc...
         */

        mojoUnderTest.setTestMethods(Collections.singletonList("test2"));
        mojoUnderTest.setTestClassesNames(Collections.singletonList("example.TestSuiteExample"));
        mojoUnderTest.setAmplifiers(Collections.singletonList("TestDataMutator"));
        mojoUnderTest.setTestCriterion("JacocoCoverageSelector");
        mojoUnderTest.setIteration(1);
        mojoUnderTest.setOutputPath("target/dspot-output");
        mojoUnderTest.execute();

        assertTrue(new File("target/dspot-output/example.TestSuiteExample_jacoco_instr_coverage_report.txt").exists());
    }

    @Test
    public void testTargetingSpecificTestMethodsFromCsvFile() throws Exception {

        /*
            In this configuration, we execute dspot on a specific test methods read from a csv file
            In order to obtain result, we set up also amplifiers and selector
            We verify the result, i.e. the report txt

            In this configuration, we retrieve from the pom, the information we need, e.g. source folders etc...
         */

        mojoUnderTest.setPathToTestListCsv("src/test/resources/test-projects/test-selection.csv");
        mojoUnderTest.setAmplifiers(Collections.singletonList("TestDataMutator"));
        mojoUnderTest.setTestCriterion("JacocoCoverageSelector");
        mojoUnderTest.setIteration(1);
        mojoUnderTest.setOutputPath("target/dspot-output");
        mojoUnderTest.execute();

        assertTrue(new File("target/dspot-output/example.TestSuiteExample_jacoco_instr_coverage_report.txt").exists());
    }
}
