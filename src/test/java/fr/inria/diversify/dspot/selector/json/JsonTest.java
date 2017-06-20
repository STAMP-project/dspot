package fr.inria.diversify.dspot.selector.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private final String expectedJson = "{" + "\n"  +
            "  \"nbMutantKilledOriginally\": 23," + "\n"  +
            "  \"name\": \"MyTestClass\"," + "\n"  +
            "  \"nbOriginalTestCases\": 1," + "\n" +
            "  \"testCases\": [" + "\n"  +
            "    {" + "\n"  +
            "      \"name\": \"myTestCase\"," + "\n"  +
            "      \"nbAssertionAdded\": 1," + "\n"  +
            "      \"nbInputAdded\": 1," + "\n"  +
            "      \"nbMutantKilled\": 1," + "\n"  +
            "      \"mutantsKilled\": [" + "\n"  +
            "        {" + "\n"  +
            "          \"ID\": \"IdMutant\"," + "\n"  +
            "          \"lineNumber\": 1," + "\n"  +
            "          \"locationMethod\": \"method\"" + "\n"  +
            "        }" + "\n"  +
            "      ]" + "\n"  +
            "    }" + "\n"  +
            "  ]" + "\n"  +
            "}";
}
