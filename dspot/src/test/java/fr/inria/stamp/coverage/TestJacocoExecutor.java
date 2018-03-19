package fr.inria.stamp.coverage;

import fr.inria.Utils;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class TestJacocoExecutor {

	@Test
	public void testJacocoExecutorOnJMockit() throws Exception {
		Utils.init("src/test/resources/jmockit/mock.properties");

		final CtClass<?> jmockitTest = Utils.findClass("org.baeldung.mocks.jmockit.LoginControllerIntegrationTest");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration(), jmockitTest);

		CoverageResults coverageResults = jacocoExecutor.executeJacoco(jmockitTest);
		assertTrue(60 <= coverageResults.instructionsCovered &&
				coverageResults.instructionsCovered <= 70);
		assertEquals(81, coverageResults.instructionsTotal);
	}

	@Test
	public void testJacocoExecutorOnEasyMock() throws Exception {
		Utils.init("src/test/resources/easymock/mock.properties");

		final CtClass<?> easyMockTest = Utils.findClass("org.baeldung.mocks.easymock.LoginControllerIntegrationTest");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration(), easyMockTest);

		CoverageResults coverageResults = jacocoExecutor.executeJacoco(easyMockTest);
		assertTrue(50 <= coverageResults.instructionsCovered &&
				coverageResults.instructionsCovered <= 60);
		assertEquals(81, coverageResults.instructionsTotal);
	}

	@Test
	public void testJacocoExecutor() throws Exception {
		Utils.init("src/test/resources/test-projects/test-projects.properties");
		final CtClass<?> testClass = Utils.findClass("example.TestSuiteExample");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration(), testClass);
		final CoverageResults coverageResults = jacocoExecutor.executeJacoco(testClass);
		assertEquals(33, coverageResults.instructionsCovered);
		assertEquals(37, coverageResults.instructionsTotal);
	}

	/**
	 * WARNING: The jacoco executor can not run mockito see: https://github.com/mockito/mockito/issues/969
	 * TODO: fixme
	 */
	@Test
	@Ignore
	public void testJacocoExecutorOnMockito() throws Exception {
		Utils.init("src/test/resources/mockito/mockito.properties");
		final CtClass<?> testClass = Utils.findClass("info.sanaulla.dal.BookDALTest");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration(), testClass);
		final CoverageResults coverageResults = jacocoExecutor.executeJacoco(testClass);
		assertEquals(0, coverageResults.instructionsCovered); // TODO not able to run mockito test
		assertEquals(65, coverageResults.instructionsTotal);
	}

	/**
	 * WARNING: The jacoco executor can not run mockito see: https://github.com/mockito/mockito/issues/969
	 * TODO: fixme
	 */
	@Test
	@Ignore
	public void testJacocoExecutorOnMockito2() throws Exception {
		Utils.init("src/test/resources/mockito2/mock.properties");

		final CtClass<?> mockitoTest = Utils.findClass("org.baeldung.mocks.mockito.LoginControllerIntegrationTest");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration(), mockitoTest);

		CoverageResults coverageResults = jacocoExecutor.executeJacoco(mockitoTest);
		assertEquals(0, coverageResults.instructionsCovered); // TODO not able to run mocked test
		assertEquals(78, coverageResults.instructionsTotal);
	}

}