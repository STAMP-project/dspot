package fr.inria.diversify.dspot;

import fr.inria.Utils;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.AbstractTest;
import fr.inria.stamp.test.launcher.TestLauncher;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;


import static fr.inria.diversify.utils.AmplificationHelper.PATH_SEPARATOR;
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
		final InputProgram program = Utils.getInputProgram();
		final String classpath = program.getProgramDir() + program.getClassesDir() + PATH_SEPARATOR +
				program.getProgramDir() + program.getTestClassesDir() + PATH_SEPARATOR +
				Utils.getBuilder().buildClasspath(program.getProgramDir());

		final TestListener result = TestLauncher.runFromSpoonNodes(
				Utils.getInputConfiguration(),
				classpath,
				classUsingResources, classUsingResources.getMethodsByName("testResources"));

		assertTrue(result.getFailingTests().isEmpty());
		assertEquals(1, result.getRunningTests().size());
	}

}
