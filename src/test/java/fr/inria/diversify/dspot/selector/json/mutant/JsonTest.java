package fr.inria.diversify.dspot.selector.json.mutant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.dspot.selector.json.mutant.MutantJSON;
import fr.inria.diversify.dspot.selector.json.mutant.TestCaseJSON;
import fr.inria.diversify.dspot.selector.json.mutant.TestClassJSON;
import org.junit.Test;

import java.io.*;
import java.util.Collections;

import static fr.inria.diversify.dspot.AbstractTest.nl;
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

    private final String expectedJson = "{" + "" + nl  +
            "  \"nbMutantKilledOriginally\": 23," + "" + nl  +
            "  \"name\": \"MyTestClass\"," + "" + nl  +
            "  \"nbOriginalTestCases\": 1," + "" + nl +
            "  \"testCases\": [" + "" + nl  +
            "    {" + "" + nl  +
            "      \"name\": \"myTestCase\"," + "" + nl  +
            "      \"nbAssertionAdded\": 1," + "" + nl  +
            "      \"nbInputAdded\": 1," + "" + nl  +
            "      \"nbMutantKilled\": 1," + "" + nl  +
            "      \"mutantsKilled\": [" + "" + nl  +
            "        {" + "" + nl  +
            "          \"ID\": \"IdMutant\"," + "" + nl  +
            "          \"lineNumber\": 1," + "" + nl  +
            "          \"locationMethod\": \"method\"" + "" + nl  +
            "        }" + "" + nl  +
            "      ]" + "" + nl  +
            "    }" + "" + nl  +
            "  ]" + "" + nl  +
            "}";
}
