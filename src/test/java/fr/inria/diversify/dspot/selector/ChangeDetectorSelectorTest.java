package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.runner.InputConfiguration;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/08/17
 */
public class ChangeDetectorSelectorTest {

	// TODO this is not deterministic
	@Test
	public void test() throws Exception, InvalidSdkException {

		final String configurationPath = "src/test/resources/regression/test-projects_0/test-projects.properties";
		final ChangeDetectorSelector changeDetectorSelector = new ChangeDetectorSelector();

		final InputConfiguration configuration = new InputConfiguration(configurationPath);
		final DSpot dSpot = new DSpot(configuration, 1,
				Collections.singletonList(new StatementAdd()),
				changeDetectorSelector);
		assertEquals(6, dSpot.getInputProgram().getFactory().Type().get("example.TestSuiteExample").getMethods().size());
		final CtType<?> ctType = dSpot.amplifyTest("example.TestSuiteExample").get(0); // TODO
		assertTrue(dSpot.getInputProgram().getFactory().Type().get("example.TestSuiteExample").getMethods().size() < ctType.getMethods().size());
	}
}
