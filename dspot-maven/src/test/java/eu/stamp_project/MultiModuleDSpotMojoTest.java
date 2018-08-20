package eu.stamp_project;

import eu.stamp_project.program.ConstantsProperties;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.codehaus.plexus.PlexusTestCase.getBasedir;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/08/18
 */
public class MultiModuleDSpotMojoTest {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    /*
        The mojo under test
     */
    private DSpotMojo mojoUnderTest;

    @Test
    public void testOnMultiModuleParent() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/multi-module/");
        mojoUnderTest = (DSpotMojo) mojoRule.lookupConfiguredMojo(testPom, "amplify-unit-tests");
        final Properties properties = mojoUnderTest.initializeProperties();
        assertNotNull(properties.get(ConstantsProperties.PROJECT_ROOT_PATH.getName()));
        assertEquals(testPom.getAbsolutePath(), properties.get(ConstantsProperties.PROJECT_ROOT_PATH.getName()));
        assertNotNull(properties.get(ConstantsProperties.SRC_CODE.getName()));
        assertEquals(testPom.getAbsolutePath() + "/src/main/java", properties.get(ConstantsProperties.SRC_CODE.getName()));
        assertNotNull(properties.get(ConstantsProperties.TEST_SRC_CODE.getName()));
        assertEquals(testPom.getAbsolutePath() + "/src/test/java", properties.get(ConstantsProperties.TEST_SRC_CODE.getName()));
        assertNotNull(properties.get(ConstantsProperties.SRC_CLASSES.getName()));
        assertEquals(testPom.getAbsolutePath() + "/target/classes", properties.get(ConstantsProperties.SRC_CLASSES.getName()));
        assertNotNull(properties.get(ConstantsProperties.TEST_CLASSES.getName()));
        assertEquals(testPom.getAbsolutePath() + "/target/test-classes", properties.get(ConstantsProperties.TEST_CLASSES.getName()));
        assertNotNull(properties.get(ConstantsProperties.MODULE.getName()));
        assertEquals("", properties.get(ConstantsProperties.MODULE.getName()));
    }

}
