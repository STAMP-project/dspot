package fr.inria.diversify.dspot.selector;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.runner.InputConfiguration;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/08/17
 */
public class RegressionSelectorTest {

	@Test
	public void test() throws Exception, InvalidSdkException {

		final String configurationPath = "src/test/resources/regression/test-projects_0/test-projects.properties";
		final RegressionSelector regressionSelector = new RegressionSelector(
				configurationPath, "src/test/resources/regression/test-projects_1");

		final InputConfiguration configuration = new InputConfiguration(configurationPath);
		final DSpot dSpot = new DSpot(configuration, 1,
				Collections.singletonList(new StatementAdd()),
				regressionSelector);
		try {
			final CtType ctType = dSpot.amplifyTest("example.TestSuiteExample"); // TODO 
			fail();
		} catch (RuntimeException e) {

		}
	}
}
