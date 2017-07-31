package fr.inria.stamp.coverage;

import fr.inria.diversify.util.Log;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class CoverageResults {

	public final int instructionsCovered;

	public final int instructionsTotal;

	public CoverageResults(int instructionsCovered, int instructionsTotal) {
		this.instructionsCovered = instructionsCovered;
		this.instructionsTotal = instructionsTotal;
	}

	public boolean isBetterThan(CoverageResults that) {
		if (that == null) {
			return true;
		}
		double percCoverageThis = ((double) this.instructionsCovered / (double) this.instructionsTotal);
		double percCoverageThat = ((double) that.instructionsCovered / (double) that.instructionsTotal);
		return percCoverageThis >= percCoverageThat;
	}

}
