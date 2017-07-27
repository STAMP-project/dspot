package fr.inria.stamp.coverage;

import fr.inria.diversify.log.branch.Coverage;
import org.kevoree.log.Log;

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

	@Override
	public String toString() {
		return "CoverageResults{" +
				"instructionsCovered=" + instructionsCovered +
				", instructionsTotal=" + instructionsTotal +
				'}';
	}

	public boolean isBetterThan(CoverageResults that) {
		if (that == null) {
			return true;
		}
		Log.debug("that is not null");
		int percCoverageThis = (int) ((double) this.instructionsCovered / (double) this.instructionsTotal);
		int percCoverageThat = (int) ((double) that.instructionsCovered / (double) that.instructionsTotal);
		if (percCoverageThat == percCoverageThis) {
			return this.instructionsCovered > that.instructionsCovered;
		} else {
			return percCoverageThis > percCoverageThat;
		}
	}

}
