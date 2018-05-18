package eu.stamp_project.dspot;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.dspot.amplifier.StatementAdd;
import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import eu.stamp_project.utils.sosiefier.InputProgram;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/04/17
 */
public class DSpotMockedTest extends AbstractTest {

	@Test
	public void test() throws Exception {

        /*
			Test the whole dspot procedure.
         */
		ValueCreator.count = 0;
		AmplificationHelper.setSeedRandom(23L);
		InputConfiguration configuration = new InputConfiguration(getPathToPropertiesFile());
		InputProgram program = new InputProgram();
		configuration.setInputProgram(program);
		DSpot dspot = new DSpot(configuration, 1,
				Arrays.asList(new StatementAdd())
		);
		try {
			FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
		} catch (Exception ignored) {

		}
		assertEquals(6, dspot.getInputConfiguration().getFactory().Class().get("info.sanaulla.dal.BookDALTest").getMethods().size());

		EntryPoint.verbose = true;

		CtType<?> amplifiedTest = dspot.amplifyTest("info.sanaulla.dal.BookDALTest", Collections.singletonList("testGetBook"));

		assertEquals(1, amplifiedTest.getMethods().stream().filter(AmplificationChecker::isTest).count());
		System.out.println(amplifiedTest);
		assertTrue(!amplifiedTest.getMethodsByName("testGetBook_sd8").isEmpty());
	}

	@Override
	public String getPathToPropertiesFile() {
		return "src/test/resources/mockito/mockito.properties";
	}
}
