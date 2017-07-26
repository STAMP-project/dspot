package fr.inria.stamp.coverage;

import fr.inria.diversify.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class TestJacocoExecutor {

	@Test
	public void testJacocoExecutor() throws Exception {
		Utils.init("src/test/resources/test-projects/test-projects.properties");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram());
		final CoverageResults coverageResults = jacocoExecutor.executeJacoco("example.TestSuiteExample");
		assertEquals(33, coverageResults.instructionsCovered);
		assertEquals(37, coverageResults.instructionsTotal);
	}
}