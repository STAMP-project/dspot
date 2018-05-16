package eu.stamp_project.dspot.selector.json.coverage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/07/17
 */
public class JsonTest {

	@Test
	public void testJson() throws Exception {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		final TestClassJSON classJSON = new TestClassJSON("example.TestSuiteExample", 23,
				80, 100,
				100, 100
		);

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

	private final String expectedJson = "{\n" +
			"  \"name\": \"example.TestSuiteExample\",\n" +
			"  \"nbOriginalTestCases\": 23,\n" +
			"  \"initialInstructionCovered\": 80,\n" +
			"  \"initialInstructionTotal\": 100,\n" +
			"  \"percentageinitialInstructionCovered\": 80.0,\n" +
			"  \"amplifiedInstructionCovered\": 100,\n" +
			"  \"amplifiedInstructionTotal\": 100,\n" +
			"  \"percentageamplifiedInstructionCovered\": 100.0,\n" +
			"  \"testCases\": []\n" +
			"}";
}
