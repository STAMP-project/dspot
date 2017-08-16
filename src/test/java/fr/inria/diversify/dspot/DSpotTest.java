package fr.inria.diversify.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.support.json.ProjectTimeJSON;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/13/16
 */
public class DSpotTest extends MavenAbstractTest {

    @Test
    public void test() throws Exception, InvalidSdkException {

        /*
            Test the whole dspot procedure.
                It results with 24 methods: 18 amplified tests + 6 original tests.
         */
        ValueCreator.count = 0;
        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        try {
            FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        } catch (Exception ignored) {

        }
        DSpot dspot = new DSpot(configuration);

        addMavenHomeToPropertiesFile();

        assertFalse(new File(configuration.getOutputDirectory() + "/test-projects.json").exists());

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");

        assertEquals(24, amplifiedTest.getMethods().size());
        assertEquals(expectedAmplifiedBody, ((CtMethod<?>)amplifiedTest.getMethodsByName("test4_sd1355_sd1358").get(0)).getBody().toString());

        final File file = new File(configuration.getOutputDirectory() + "/test-projects.json");
        assertTrue(file.exists());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ProjectTimeJSON projectTimeJSON = gson.fromJson(new FileReader(file), ProjectTimeJSON.class);
        assertEquals("test-projects", projectTimeJSON.projectName);
        assertEquals(1, projectTimeJSON.classTimes.size());
        assertEquals("example.TestSuiteExample", projectTimeJSON.classTimes.get(0).fullQualifiedName);
        // do not test the time...

        removeHomFromPropertiesFile();
    }

    private final String expectedAmplifiedBody = "{" + nl  +
            "    int index_312 = -1787921271;" + nl  +
            "    java.lang.String s_311 = \"q5 w=#@fx)l0pr;puH(&\";" + nl  +
            "    // AssertGenerator add assertion" + nl  +
            "    org.junit.Assert.assertEquals(\"q5 w=#@fx)l0pr;puH(&\", s_311);" + nl  +
            "    example.Example gen_o36 = new example.Example();" + nl  +
            "    example.Example ex = new example.Example();" + nl  +
            "    java.lang.String s = \"abcd\";" + nl  +
            "    // AssertGenerator add assertion" + nl  +
            "    org.junit.Assert.assertEquals(\"abcd\", s);" + nl  +
            "    // AssertGenerator create local variable with return value of invocation" + nl  +
            "    char o_test4_sd1355_sd1358__8 = ex.charAt(s, 12);" + nl  +
            "    // AssertGenerator add assertion" + nl  +
            "    org.junit.Assert.assertEquals('d', ((char) (o_test4_sd1355_sd1358__8)));" + nl  +
            "    // AssertGenerator create local variable with return value of invocation" + nl  +
            "    char o_test4_sd1355_sd1358__9 = // StatementAdd: add invocation of a method" + nl  +
            "    ex.charAt(s_311, index_312);" + nl  +
            "    // AssertGenerator add assertion" + nl  +
            "    org.junit.Assert.assertEquals('q', ((char) (o_test4_sd1355_sd1358__9)));" + nl  +
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
