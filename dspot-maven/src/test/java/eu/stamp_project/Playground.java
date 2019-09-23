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
public class Playground {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    /*
        The mojo under test
     */
    private DSpotMojo mojoUnderTest;

    @Before
    public void setUp() throws Exception {
        File testPom = new File("/tmp/TestAutomationFramework/Core");
        mojoUnderTest = (DSpotMojo) mojoRule.lookupConfiguredMojo(testPom , "amplify-unit-tests");
        mojoUnderTest.setVerbose(true);
    }

    @Test
    public void testDefaultConfiguration() throws Exception {

        /*
            We just need to specify a path to properties to execute dspot, nothing more.
            WEAK ORACLE: we just execute the mojo, if everything is fine, it is okay
         */
        mojoUnderTest.setPathPitResult("src/test/resources/test-projects/originalpit/mutations.csv");
        mojoUnderTest.setTestCriterion("PitMutantScoreSelector");
        mojoUnderTest.setOutputPath("target/dspot-output");

        mojoUnderTest.execute();
    }

}
