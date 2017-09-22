package fr.inria.diversify.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import fr.inria.diversify.dspot.support.json.ProjectTimeJSON;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.sosiefier.runner.InputConfiguration;
import fr.inria.diversify.sosiefier.runner.InputProgram;
import fr.inria.diversify.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
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
    public void test() throws Exception {

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
        DSpot dspot = new DSpot(configuration,
                1,
                Collections.singletonList(new StatementAdd()),
                new JacocoCoverageSelector()
        );

        assertFalse(new File(configuration.getOutputDirectory() + "/test-projects.json").exists());

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample").get(0);

        assertEquals(21, amplifiedTest.getMethods().size());
        assertEquals(expectedAmplifiedBody, ((CtMethod<?>)amplifiedTest.getMethodsByName("test2_sd1").get(0)).getBody().toString());

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
            "    int index_1 = -1348236471;" + nl +
            "    java.lang.String s_0 = \"-*k},GdhscbCS@!x*zH_\";" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(\"-*k},GdhscbCS@!x*zH_\", s_0);" + nl +
            "    example.Example ex = new example.Example();" + nl +
            "    // AssertGenerator create local variable with return value of invocation" + nl +
            "    char o_test2_sd1__5 = ex.charAt(\"abcd\", 3);" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals('d', ((char) (o_test2_sd1__5)));" + nl +
            "    // AssertGenerator create local variable with return value of invocation" + nl +
            "    char o_test2_sd1__6 = // StatementAdd: add invocation of a method" + nl +
            "    ex.charAt(s_0, index_1);" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals('-', ((char) (o_test2_sd1__6)));" + nl +
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
