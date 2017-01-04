package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.stream.Collectors;

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
                It results with 18 methods: 7 manual + 13 amplified.
                The test consist of assert that the manual test remains, and there is an amplified version
         */

        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration);

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");
        assertEquals(20, amplifiedTest.getMethods().size());
        assertEquals(originalTestBody, amplifiedTest.getMethod("test1").getBody().toString());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test1_cf24").getBody().toString());
    }

    private final String pathToPropertiesFile = "src/test/resources/test-projects/test-projects.properties";

    private final String nl = System.getProperty("line.separator");

    private final String originalTestBody = "{" + nl +
            "    example.Example ex = new example.Example();" + nl +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl +
            "}";

    private final String expectedAmplifiedBody = "{" + nl +
            "    example.Example ex = new example.Example();" + nl +
            "    int vc_5 = 1635508580;" + nl +
            "    junit.framework.Assert.assertEquals(vc_5, 1635508580);" + nl +
            "    java.lang.String vc_0 = \"abcd\";" + nl +
            "    junit.framework.Assert.assertEquals(vc_0, \"abcd\");" + nl +
            "    example.Example vc_1 = new example.Example();" + nl +
            "    char o_test1_cf24__6 = vc_1.charAt(vc_0, vc_5);" + nl +
            "    junit.framework.Assert.assertEquals(o_test1_cf24__6, 'd');" + nl +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl +
            "}";

    private static String originalProperties;

    @Before
    public void setUp() throws Exception {
        addMavenHomeToPropertiesFile();
    }

    @After
    public void tearDown() throws Exception {
        removeHomFromPropertiesFile();
    }

    // hack to add maven.home to the properties automatically for travis. For local, the test will clean
    private void addMavenHomeToPropertiesFile() {
        try (BufferedReader buffer = new BufferedReader(new FileReader(pathToPropertiesFile))) {
            originalProperties = buffer.lines().collect(Collectors.joining(nl));
            System.out.println(originalProperties);
        } catch (IOException ignored) {
            //ignored
        }
        final String mavenHome = Utils.buildMavenHome();
        if (mavenHome != null) {
            try(FileWriter writer = new FileWriter(pathToPropertiesFile, true)) {
                writer.write(nl + "maven.home=" + mavenHome + nl);
            } catch (IOException ignored) {
                //ignored
            }
        }
    }

    private void removeHomFromPropertiesFile() {
        try(FileWriter writer = new FileWriter(pathToPropertiesFile, false)) {
            writer.write(originalProperties);
        } catch (IOException ignored) {
            //ignored
        }
    }
}
