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
        assertEquals(18, amplifiedTest.getMethods().size());
        assertEquals(originalTestBody, amplifiedTest.getMethod("test1").getBody().toString());

        amplifiedTest.getMethods();

        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test1_cf6_cf34_cf167").getBody().toString());
    }

    private final String pathToPropertiesFile = "src/test/resources/test-projects/test-projects.properties";

    private final String nl = System.getProperty("line.separator");

    private final String originalTestBody = "{" + nl  +
            "    example.Example ex = new example.Example();" + nl  +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl  +
            "}";

    private final String expectedAmplifiedBody = "{" + nl  +
            "    example.Example ex = new example.Example();" + nl  +
            "    int vc_5 = -619987209;" + nl  +
            "    junit.framework.Assert.assertEquals(vc_5, -619987209);" + nl  +
            "    java.lang.String vc_0 = \"abcd\";" + nl  +
            "    junit.framework.Assert.assertEquals(vc_0, \"abcd\");" + nl  +
            "    char o_test1_cf6_cf34_cf167__5 = ex.charAt(vc_0, vc_5);" + nl  +
            "    junit.framework.Assert.assertEquals(o_test1_cf6_cf34_cf167__5, 'a');" + nl  +
            "    char o_test1_cf6_cf34_cf167__6 = ex.charAt(vc_0, vc_5);" + nl  +
            "    junit.framework.Assert.assertEquals(o_test1_cf6_cf34_cf167__6, 'a');" + nl  +
            "    int vc_29 = 995075168;" + nl  +
            "    junit.framework.Assert.assertEquals(vc_29, 995075168);" + nl  +
            "    java.lang.String vc_8 = \"abcd\";" + nl  +
            "    junit.framework.Assert.assertEquals(vc_8, \"abcd\");" + nl  +
            "    char o_test1_cf6_cf34_cf167__9 = ex.charAt(vc_8, vc_29);" + nl  +
            "    junit.framework.Assert.assertEquals(o_test1_cf6_cf34_cf167__9, 'd');" + nl  +
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
