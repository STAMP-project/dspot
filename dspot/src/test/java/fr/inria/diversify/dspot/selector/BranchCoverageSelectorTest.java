package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/07/17
 */
public class BranchCoverageSelectorTest {

	@Test
	public void testDSpotWithBranchCoverageSelector() throws Exception {
		long time = System.currentTimeMillis();
		AmplificationHelper.setSeedRandom(23L);
		InputConfiguration configuration = new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
		DSpot dspot = new DSpot(configuration, 1, Collections.singletonList(new StatementAdd()) ,
			new BranchCoverageTestSelector(1000));
		dspot.amplifyTest("example.TestSuiteExample", Collections.singletonList("test2"));
		System.out.println(System.currentTimeMillis() - time);
	}

}
