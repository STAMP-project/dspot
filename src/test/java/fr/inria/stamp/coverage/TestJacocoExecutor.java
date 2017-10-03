package fr.inria.stamp.coverage;

import fr.inria.diversify.Utils;
import org.junit.After;
import org.junit.Before;
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

	@Before
	public void setUp() throws Exception {
		Utils.reset();
	}

	@After
	public void tearDown() throws Exception {
		Utils.reset();
	}


	@Test
	@Ignore // TODO We may need a specific runner.
	public void testJacocoExecutorOnJMockit() throws Exception {
		Utils.reset();
		Utils.init("src/test/resources/jmockit/mock.properties");

		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration());
		final CtClass<?> jmockitTest = Utils.findClass("org.baeldung.mocks.jmockit.LoginControllerIntegrationTest");

		CoverageResults coverageResults = jacocoExecutor.executeJacoco(jmockitTest);
		assertTrue(60 <= coverageResults.instructionsCovered &&
				coverageResults.instructionsCovered <= 70);
		assertEquals(78, coverageResults.instructionsTotal);
	}

	//TODO fix the range
	@Test
	public void testJacocoExecutorOnEasyMock() throws Exception {
		Utils.reset();
		Utils.init("src/test/resources/easymock/mock.properties");

		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration());
		final CtClass<?> easyMockTest = Utils.findClass("org.baeldung.mocks.easymock.LoginControllerIntegrationTest");

		CoverageResults coverageResults = jacocoExecutor.executeJacoco(easyMockTest);
		assertTrue(50 <= coverageResults.instructionsCovered &&
				coverageResults.instructionsCovered <= 60);
		assertEquals(78, coverageResults.instructionsTotal);
	}

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
	@Ignore
	public void testJacocoExecutorOnMockito() throws Exception {
		Utils.reset();
		Utils.init("src/test/resources/mockito/mockito.properties");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration());
		final CoverageResults coverageResults = jacocoExecutor.executeJacoco(Utils.findClass("info.sanaulla.dal.BookDALTest"));
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
		Utils.reset();
		Utils.init("src/test/resources/mock/mock.properties");

		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram(), Utils.getInputConfiguration());
		final CtClass<?> mockitoTest = Utils.findClass("org.baeldung.mocks.mockito.LoginControllerIntegrationTest");

		CoverageResults coverageResults = jacocoExecutor.executeJacoco(mockitoTest);
		assertEquals(0, coverageResults.instructionsCovered); // TODO not able to run mocked test
		assertEquals(78, coverageResults.instructionsTotal);
	}

}