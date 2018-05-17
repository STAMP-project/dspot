package eu.stamp_project.dspot.selector.json.mutant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;

import java.io.*;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/20/17
 */
public class JsonTest {

    @Test
    public void testJSON() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        TestClassJSON classJSON = new TestClassJSON(23, "MyTestClass", 1);
        classJSON.addTestCase(new TestCaseJSON("myTestCase", 1, 1,
                Collections.singletonList(new MutantJSON("IdMutant", 1, "method"))));
        String actualJson = gson.toJson(classJSON);

        assertEquals(expectedJson, actualJson);

        try (FileWriter writer = new FileWriter("target/test.json")) {
            writer.write(actualJson);
        } catch (IOException ignored) {
            //should not happen
            fail();
        }

        TestClassJSON testClassJSON = gson.fromJson(new FileReader(new File("target/test.json")), TestClassJSON.class);

        actualJson = gson.toJson(testClassJSON);
        assertEquals(expectedJson, actualJson);
    }

    // do not use system separator because gson generate json with \n

    private final String expectedJson = "{" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "  \"nbMutantKilledOriginally\": 23," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "  \"name\": \"MyTestClass\"," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "  \"nbOriginalTestCases\": 1," + "" + AmplificationHelper.LINE_SEPARATOR +
            "  \"testCases\": [" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "    {" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "      \"name\": \"myTestCase\"," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "      \"nbAssertionAdded\": 1," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "      \"nbInputAdded\": 1," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "      \"nbMutantKilled\": 1," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "      \"mutantsKilled\": [" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "        {" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "          \"ID\": \"IdMutant\"," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "          \"lineNumber\": 1," + "" + AmplificationHelper.LINE_SEPARATOR  +
            "          \"locationMethod\": \"method\"" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "        }" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "      ]" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "    }" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "  ]" + "" + AmplificationHelper.LINE_SEPARATOR  +
            "}";
}
