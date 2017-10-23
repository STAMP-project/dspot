package fr.inria.diversify.dspot;

import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/04/17
 */
public class DSpotMockedTest extends MavenAbstractTest {

	@Test
	public void test() throws Exception {

        /*
			Test the whole dspot procedure.
         */
		ValueCreator.count = 0;
		AmplificationHelper.setSeedRandom(23L);
		InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
		InputProgram program = new InputProgram();
		configuration.setInputProgram(program);
		DSpot dspot = new DSpot(configuration, 1);
		try {
			FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
		} catch (Exception ignored) {

		}
		assertEquals(6, dspot.getInputProgram().getFactory().Class().get("info.sanaulla.dal.BookDALTest").getMethods().size());

		CtType<?> amplifiedTest = dspot.amplifyTest("info.sanaulla.dal.BookDALTest", Collections.singletonList("testAddBook"));

//		assertEquals(8, amplifiedTest.getMethods().size());
		assertTrue(amplifiedTest.getMethods().size() > 6);//TODO Fix it
	}

	@Override
	public String getPathToPropertiesFile() {
		return "src/test/resources/mockito/mockito.properties";
	}
}
