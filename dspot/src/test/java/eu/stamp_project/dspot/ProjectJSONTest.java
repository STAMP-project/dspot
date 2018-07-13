package eu.stamp_project.dspot;

import eu.stamp_project.dspot.amplifier.TestDataMutator;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class ProjectJSONTest {

	@Before
	public void setUp() throws Exception {
		try {
			FileUtils.forceDelete(new File("target/dspot/"));
		} catch (Exception ignored) {

		}
	}

	@Test
	public void test() throws Exception {

		final File file = new File("target/trash/sample.json");
		if (file.exists()) {
			file.delete();
		}

		DSpot dspot = new DSpot(InputConfiguration.initialize("src/test/resources/sample/sample.properties"),
				1,
				Collections.singletonList(new TestDataMutator()),
				new JacocoCoverageSelector()
		);

		dspot.amplifyTest("fr.inria.amp.TestJavaPoet");
		try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
			final String jsonAsString = buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
			assertTrue(jsonAsString.startsWith(expectedFirstProjectJSON[0]));
			assertTrue(jsonAsString.endsWith(expectedFirstProjectJSON[1]));
		}
		dspot.amplifyTest("fr.inria.mutation.ClassUnderTestTest");
		try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
			final String jsonAsString = buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
			assertTrue(jsonAsString.startsWith(expectedFirstProjectJSON[0]));
			assertTrue(jsonAsString.contains(expectedFirstProjectJSON[2]));
			assertTrue(jsonAsString.endsWith(expectedFirstProjectJSON[3]));
		}

		dspot.amplifyTest("fr.inria.amp.TestJavaPoet");
		try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
			final String jsonAsString = buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
			assertTrue(jsonAsString.startsWith(expectedFirstProjectJSON[0]));
			assertTrue(jsonAsString.endsWith(expectedFirstProjectJSON[1]));
		}
	}

	//we cannot predict the time of the computation of dspot, we split the output string into two parts: head and tail.

	private static final String[] expectedFirstProjectJSON = new String[]{
			"{" + AmplificationHelper.LINE_SEPARATOR +
					"  \"classTimes\": [" + AmplificationHelper.LINE_SEPARATOR +
					"    {" + AmplificationHelper.LINE_SEPARATOR +
					"      \"fullQualifiedName\": \"fr.inria.amp.TestJavaPoet\"," + AmplificationHelper.LINE_SEPARATOR +
					"      \"timeInMs\": ",
			AmplificationHelper.LINE_SEPARATOR +
					"    }" + AmplificationHelper.LINE_SEPARATOR +
					"  ]," + AmplificationHelper.LINE_SEPARATOR +
					"  \"projectName\": \"sample\"" + AmplificationHelper.LINE_SEPARATOR +
					"}",
			"    }," + AmplificationHelper.LINE_SEPARATOR +
					"    {" + AmplificationHelper.LINE_SEPARATOR +
					"      \"fullQualifiedName\": \"fr.inria.mutation.ClassUnderTestTest\"," + AmplificationHelper.LINE_SEPARATOR +
					"      \"timeInMs\": ",
			"    }" + AmplificationHelper.LINE_SEPARATOR +
					"  ]," + AmplificationHelper.LINE_SEPARATOR +
					"  \"projectName\": \"sample\"" + AmplificationHelper.LINE_SEPARATOR +
					"}"
	};
}
