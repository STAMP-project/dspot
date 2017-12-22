package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/17
 */
public class TakeAllSelectorTest {

	@Test
	public void test() throws Exception {

		try {
			FileUtils.deleteDirectory(new File("target/trash"));
		} catch (Exception ignored) {
			//ignored
		}
		AmplificationHelper.setSeedRandom(23L);
		InputConfiguration configuration = new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
		DSpot dspot = new DSpot(configuration, 1, Collections.singletonList(new StatementAdd()),
				new TakeAllSelector());
		assertEquals(6, dspot.getInputProgram().getFactory().Class().get("example.TestSuiteExample").getMethods().size());
		final CtType<?> amplifiedTest = dspot.amplifyTest("example.TestSuiteExample", Collections.singletonList("test2"));
		assertEquals(2, amplifiedTest.getMethods().size());
	}

}
