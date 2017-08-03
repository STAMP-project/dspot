package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/07/17
 */
public class JacocoCoverageSelectorTest {

	public static final String nl = System.getProperty("line.separator");

	@Test
	public void testDSpotWithJacocoCoverageSelector() throws Exception, InvalidSdkException {
		try {
			FileUtils.deleteDirectory(new File("dspot-out"));
		} catch (Exception ignored) {
			//ignored
		}
		AmplificationHelper.setSeedRandom(23L);
		InputConfiguration configuration = new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
		DSpot dspot = new DSpot(configuration, new JacocoCoverageSelector());
		dspot.amplifyTest("example.TestSuiteExample", Collections.singletonList("test2"));

		try (BufferedReader buffer = new BufferedReader(new FileReader(configuration.getOutputDirectory() +
				"example.TestSuiteExample_jacoco_instr_coverage_report.txt"
		))) {
			assertEquals(expectedReport, buffer.lines().collect(Collectors.joining(nl)));
		}
	}

	private static final String expectedReport = nl + "======= REPORT =======" + nl +
			"Initial instruction coverage: 33 / 37" + nl +
			"89.19%" + nl +
			"Amplification results with 87 amplified tests." + nl +
			"Amplified instruction coverage: 37 / 37" + nl +
			"100.00%";

}
