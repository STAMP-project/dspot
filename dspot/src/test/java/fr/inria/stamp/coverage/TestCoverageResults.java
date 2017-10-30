package fr.inria.stamp.coverage;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/07/17
 */
public class TestCoverageResults {

	@Test
	public void testCompareTo() throws Exception {
		final CoverageResults coverageResults = new CoverageResults(5, 10);
		final CoverageResults coverageResults1 = new CoverageResults(10, 10);
		final CoverageResults coverageResults2 = new CoverageResults(0, 10);
		final CoverageResults coverageResults3 = new CoverageResults(5, 5);

		assertFalse(coverageResults.isBetterThan(coverageResults1));
		assertTrue(coverageResults.isBetterThan(coverageResults2));
		assertFalse(coverageResults.isBetterThan(coverageResults3));

		assertTrue(coverageResults1.isBetterThan(coverageResults));
		assertTrue(coverageResults1.isBetterThan(coverageResults2));
		assertTrue(coverageResults1.isBetterThan(coverageResults3));

		assertFalse(coverageResults2.isBetterThan(coverageResults));
		assertFalse(coverageResults2.isBetterThan(coverageResults1));
		assertFalse(coverageResults2.isBetterThan(coverageResults3));

		assertTrue(coverageResults3.isBetterThan(coverageResults));
		assertTrue(coverageResults3.isBetterThan(coverageResults1));
		assertTrue(coverageResults3.isBetterThan(coverageResults2));

		assertTrue(coverageResults.isBetterThan(null));
	}
}
