package fr.inria.diversify.dspot.resources;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.stamp.test.launcher.TestLauncher;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/14/17
 */
public class DSpotAndResourcesTest {

	@Test
	public void test() throws Exception, InvalidSdkException {
		final InputConfiguration inputConfiguration = new InputConfiguration("src/test/resources/sample/sample.properties");
		final DSpot dSpot = new DSpot(inputConfiguration);
		InputProgram program = dSpot.getInputProgram();
		final CtClass<?> classUsingResources = program.getFactory().Class().get("fr.inria.testresources.TestResources");
		final String classpath = program.getProgramDir() + program.getClassesDir() + "/" +
				System.getProperty("path.separator") +
				program.getProgramDir() + program.getTestClassesDir() + "/";
		final TestListener result = TestLauncher.runFromSpoonNodes(
				inputConfiguration,
				classpath,
				classUsingResources, classUsingResources.getMethodsByName("testResources"));

		assertTrue(new File("src/test/resources/aResource").exists());
		assertTrue(new File("./src/test/resources/aResource").exists());
		assertTrue(new File("src/test/resources/aResourcesDirectory/anotherResource").exists());
		assertTrue(new File("./src/test/resources/aResourcesDirectory/anotherResource").exists());
		assertTrue(result.getFailingTests().isEmpty());
		assertEquals(1, result.getRunningTests().size());
		assertEquals("testResources", result.getRunningTests().get(0).getMethodName());

		dSpot.cleanResources();

		assertFalse(new File("src/test/resources/aResourcesDirectory/anotherResource").exists());
		assertFalse(new File("./src/test/resources/aResourcesDirectory/anotherResource").exists());
	}

}
