package fr.inria.diversify.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.support.json.ProjectTimeJSON;
import fr.inria.diversify.dspot.value.ValueCreator;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.stream.Collectors;

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

        assertEquals(18, amplifiedTest.getMethods().size());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test2_cf27").getBody().toString());

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

    private final String expectedAmplifiedBody = "{\n" +
            "    example.Example ex = new example.Example();\n" +
            "    // StatementAdderOnAssert create random local variable\n" +
            "    int vc_4 = -710729714;\n" +
            "    // AssertGenerator add assertion\n" +
            "    org.junit.Assert.assertEquals(vc_4, -710729714);\n" +
            "    // StatementAdderOnAssert create random local variable\n" +
            "    java.lang.String vc_3 = \"!x*zH_,y(q2 5[gpbL[{\";\n" +
            "    // AssertGenerator add assertion\n" +
            "    org.junit.Assert.assertEquals(vc_3, \"!x*zH_,y(q2 5[gpbL[{\");\n" +
            "    // StatementAdderOnAssert create random local variable\n" +
            "    example.Example vc_1 = new example.Example();\n" +
            "    // AssertGenerator create local variable with return value of invocation\n" +
            "    char o_test2_cf27__10 = // StatementAdderMethod cloned existing statement\n" +
            "    vc_1.charAt(vc_3, vc_4);\n" +
            "    // AssertGenerator add assertion\n" +
            "    org.junit.Assert.assertEquals(o_test2_cf27__10, '!');\n" +
            "    org.junit.Assert.assertEquals('d', ex.charAt(\"abcd\", 3));\n" +
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
