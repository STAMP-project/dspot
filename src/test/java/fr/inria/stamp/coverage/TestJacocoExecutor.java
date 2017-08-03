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
		Utils.reset();
		Utils.init("src/test/resources/test-projects/test-projects.properties");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration());
		final CoverageResults coverageResults = jacocoExecutor.executeJacoco(Utils.findClass("example.TestSuiteExample"));
		assertEquals(33, coverageResults.instructionsCovered);
		assertEquals(37, coverageResults.instructionsTotal);
	}

	/**
	 * WARNING: The jacoco executor can not run mockito see: https://github.com/mockito/mockito/issues/969
	 * TODO: fixme
	 */
	@Test
	public void testJacocoExecutorOnMockito() throws Exception {
		Utils.reset();
		Utils.init("src/test/resources/mockito/mockito.properties");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration());
		final CoverageResults coverageResults = jacocoExecutor.executeJacoco(Utils.findClass("info.sanaulla.dal.BookDALTest"));
		assertEquals(0, coverageResults.instructionsCovered); // TODO not able to run mockito test
		assertEquals(65, coverageResults.instructionsTotal);
	}

}