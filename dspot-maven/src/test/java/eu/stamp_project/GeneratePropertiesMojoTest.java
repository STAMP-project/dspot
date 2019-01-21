package eu.stamp_project;

import eu.stamp_project.utils.program.ConstantsProperties;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.codehaus.plexus.PlexusTestCase.getBasedir;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/01/19
 */
public class GeneratePropertiesMojoTest {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    /*
        The mojo under test
     */
    private GeneratePropertiesMojo mojoUnderTest;

    @Before
    public void setUp() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/test-projects/");
        mojoUnderTest = (GeneratePropertiesMojo) mojoRule.lookupConfiguredMojo(testPom , "generate-properties");
    }


    @Test
    public void testOnTestProjects() throws Exception {
        mojoUnderTest.execute();
        assertTrue(mojoUnderTest.getProject().getBasedir().getAbsolutePath() + mojoUnderTest.getOutputPath() + " does not exist!",
                new File(mojoUnderTest.getProject().getBasedir().getAbsolutePath() + mojoUnderTest.getOutputPath()).exists()
        );
    }


}
