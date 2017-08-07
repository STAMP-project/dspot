package fr.inria.stamp.coverage;


import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class CoverageResults {

	public final int instructionsCovered;

	public final int instructionsTotal;

	private final CoverageBuilder coverageBuilder;

	public CoverageResults(int instructionsCovered, int instructionsTotal) {
		this.instructionsCovered = instructionsCovered;
		this.instructionsTotal = instructionsTotal;
		this.coverageBuilder = null;
	}

	public CoverageResults(CoverageBuilder coverageBuilder) {
		this.coverageBuilder = coverageBuilder;
		final int[] counter = new int[2];
		coverageBuilder.getClasses().stream()
				.map(IClassCoverage::getInstructionCounter)
				.forEach(iCounter -> {
					counter[0] += iCounter.getCoveredCount();
					counter[1] += iCounter.getTotalCount();
				});
		this.instructionsCovered = counter[0];
		this.instructionsTotal = counter[1];
	}

	public CoverageBuilder getCoverageBuilder() {
		return this.coverageBuilder;
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
