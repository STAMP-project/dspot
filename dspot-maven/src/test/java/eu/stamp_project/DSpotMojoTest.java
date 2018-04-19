package eu.stamp_project;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class DSpotMojoTest extends AbstractMojoTestCase {
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// required for mojo lookups to work
		super.setUp();
	}

	/**
	 * @throws Exception
	 */
	public void testMojoGoal() throws Exception {
		File testPom = new File(getBasedir(), "../dspot/src/test/resources/test-projects/pom.xml");
		assertNotNull(testPom);

		DSpotMojo mojo = (DSpotMojo) lookupMojo("amplify-unit-tests", testPom);
		mojo.execute();

		assertNotNull(mojo);
	}
}
