package fr.inria.diversify.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.amplifier.TestDataMutatorTest;
import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import fr.inria.diversify.dspot.selector.TestSelector;
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
import java.util.Collections;
import java.util.List;

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
                Collections.singletonList(new TestDataMutator()),
                new JacocoCoverageSelector()
        );

        assertFalse(new File(configuration.getOutputDirectory() + "/test-projects.json").exists());

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample").get(0);

        assertEquals(28, amplifiedTest.getMethods().size());
        assertEquals(expectedAmplifiedBody, ((CtMethod<?>)amplifiedTest.getMethodsByName("test2").get(0)).getBody().toString());

        final File file = new File(configuration.getOutputDirectory() + "/test-projects.json");
        assertTrue(file.exists());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ProjectTimeJSON projectTimeJSON = gson.fromJson(new FileReader(file), ProjectTimeJSON.class);
        assertEquals("test-projects", projectTimeJSON.projectName);
        assertEquals(1, projectTimeJSON.classTimes.size());
        assertEquals("example.TestSuiteExample", projectTimeJSON.classTimes.get(0).fullQualifiedName);
        // do not test the time...
    }

    private final String expectedAmplifiedBody = "{\n" +
            "    example.Example ex = new example.Example();\n" +
            "    // AssertGenerator create local variable with return value of invocation\n" +
            "    char o_test2__3 = ex.charAt(\"abcd\", 3);\n" +
            "    // AssertGenerator add assertion\n" +
            "    org.junit.Assert.assertEquals('d', ((char) (o_test2__3)));\n" +
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
