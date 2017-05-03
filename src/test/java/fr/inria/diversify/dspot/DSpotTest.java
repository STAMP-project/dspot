package fr.inria.diversify.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.selector.json.TestClassJSON;
import fr.inria.diversify.dspot.support.ProjectTimeJSON;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import org.junit.Test;
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
        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration);
        try {
            FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        } catch (Exception ignored) {
            //ignored
        }
        assertFalse(new File(configuration.getOutputDirectory() + "/test-projects.json").exists());

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");

        assertEquals(18, amplifiedTest.getMethods().size());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test2_cf16").getBody().toString());

        final File file = new File(configuration.getOutputDirectory() + "/test-projects.json");
        assertTrue(file.exists());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ProjectTimeJSON projectTimeJSON = gson.fromJson(new FileReader(file), ProjectTimeJSON.class);
        assertEquals("test-projects", projectTimeJSON.projectName);
        assertEquals(1, projectTimeJSON.classTimes.size());
        assertEquals("example.TestSuiteExample", projectTimeJSON.classTimes.get(0).fullQualifiedName);
        // do not test the time...
    }

    private final String expectedAmplifiedBody = "{" + nl +
            "    example.Example ex = new example.Example();" + nl + 
            "    // StatementAdderOnAssert create random local variable" + nl + 
            "    int vc_4 = -619987209;" + nl +
            "    // AssertGenerator add assertion" + nl + 
            "    org.junit.Assert.assertEquals(vc_4, -619987209);" + nl +
            "    // StatementAdderOnAssert create literal from method" + nl + 
            "    java.lang.String String_vc_0 = \"abcd\";" + nl + 
            "    // AssertGenerator add assertion" + nl + 
            "    org.junit.Assert.assertEquals(String_vc_0, \"abcd\");" + nl + 
            "    // StatementAdderOnAssert create random local variable" + nl + 
            "    example.Example vc_1 = new example.Example();" + nl + 
            "    // AssertGenerator replace invocation" + nl + 
            "    char o_test2_cf16__9 = // StatementAdderMethod cloned existing statement" + nl + 
            "vc_1.charAt(String_vc_0, vc_4);" + nl + 
            "    // AssertGenerator add assertion" + nl + 
            "    org.junit.Assert.assertEquals(o_test2_cf16__9, 'a');" + nl +
            "    org.junit.Assert.assertEquals('d', ex.charAt(\"abcd\", 3));" + nl + 
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
