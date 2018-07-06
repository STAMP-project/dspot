package eu.stamp_project.dspot;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.program.InputConfiguration;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/14/17
 */
public class DSpotAndResourcesTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		/*
			Contract: DSpot is able to run tests that are using resources.
			 This does not mean that referenced resources by relative-path is supported.
			 Developers should not point directly resources by relative path inside their test, but rather use API
			 such as getResourceAsStream()
		 */
		final CtClass<?> classUsingResources = Utils.findClass("fr.inria.testresources.TestResources");
		final InputConfiguration configuration = Utils.getInputConfiguration();
		final String classpath = configuration.getClasspathClassesProject()
				+ AmplificationHelper.PATH_SEPARATOR +
				configuration.getDependencies();

		final TestListener result = EntryPoint.runTests(
				classpath,
				classUsingResources.getQualifiedName(),
				classUsingResources.getMethodsByName("testResources")
						.get(0)
						.getSimpleName()
		);

		assertTrue(result.getFailingTests().isEmpty());
		assertEquals(1, result.getRunningTests().size());
	}

}
