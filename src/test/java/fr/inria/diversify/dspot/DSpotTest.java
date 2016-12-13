package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/13/16
 */
public class DSpotTest {


    @Test
    public void test() throws Exception, InvalidSdkException {

        /*
            Test the whole dspot procedure.
                It results with 20 methods: 7 manual + 13 amplified.
                The test consist of assert that the manual test remains, and there is an amplified version
         */

        addMavenHomeToPropertiesFile();
        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration);

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");
        assertEquals(19, amplifiedTest.getMethods().size());
        assertEquals(originalTestBody, amplifiedTest.getMethod("test1").getBody().toString());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test1_cf5_cf236").getBody().toString());
    }

    private final String pathToPropertiesFile = "src/test/resources/test-projects/test-projects.properties";

    private final String nl = System.getProperty("line.separator");

    private final String originalTestBody = "{" + nl  +
            "    example.Example ex = new example.Example();" + nl  +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl  +
            "}";

    private final String expectedAmplifiedBody = "{" + nl  +
            "    example.Example ex = new example.Example();" + nl  +
            "    int vc_1 = 0;" + nl  +
            "    junit.framework.Assert.assertEquals(vc_1, 0);" + nl  +
            "    java.lang.String s = \"abcd\";" + nl  +
            "    junit.framework.Assert.assertEquals(s, \"abcd\");" + nl  +
            "    char o_test1_cf5_cf236__5 = ex.charAt(s, vc_1);" + nl  +
            "    junit.framework.Assert.assertEquals(o_test1_cf5_cf236__5, 'a');" + nl  +
            "    int vc_35 = 715956334;" + nl  +
            "    junit.framework.Assert.assertEquals(vc_35, 715956334);" + nl  +
            "    java.lang.String vc_10 = \"abcd\";" + nl  +
            "    junit.framework.Assert.assertEquals(vc_10, \"abcd\");" + nl  +
            "    char o_test1_cf5_cf236__8 = ex.charAt(vc_10, vc_35);" + nl  +
            "    junit.framework.Assert.assertEquals(o_test1_cf5_cf236__8, 'd');" + nl  +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl  +
            "}";

    // hack to add maven.home to the properties automatically for travis.
    private void addMavenHomeToPropertiesFile() {
        final String mavenHome = Utils.buildMavenHome();
        if (mavenHome != null) {
            try {
                FileWriter writer = new FileWriter(pathToPropertiesFile, true);
                writer.write(nl + "maven.home=" + mavenHome + nl);
                writer.close();
            } catch (IOException ignored) {
                //ignored
            }
        }
    }
}
