package eu.stamp_project.dspot.selector.json.coverage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/07/17
 */
public class TestClassJSON {

	private final String name;
	private final long nbOriginalTestCases;
	private final long initialInstructionCovered;
	private final long initialInstructionTotal;
	private final double percentageinitialInstructionCovered;
	private final long amplifiedInstructionCovered;
	private final long amplifiedInstructionTotal;
	private final double percentageamplifiedInstructionCovered;
	private List<TestCaseJSON> testCases;

	public TestClassJSON(String name, long nbOriginalTestCases,
						 long initialInstructionCovered, long initialInstructionTotal,
						 long amplifiedInstructionCovered, long amplifiedInstructionTotal) {
		this.name = name;
		this.nbOriginalTestCases = nbOriginalTestCases;
		this.testCases = new ArrayList<>();
		this.initialInstructionCovered = initialInstructionCovered;
		this.initialInstructionTotal = initialInstructionTotal;
		this.percentageinitialInstructionCovered = ((double) initialInstructionCovered / (double) initialInstructionTotal) * 100.0D;
		this.amplifiedInstructionCovered = amplifiedInstructionCovered;
		this.amplifiedInstructionTotal = amplifiedInstructionTotal;
		this.percentageamplifiedInstructionCovered = ((double) amplifiedInstructionCovered / (double) amplifiedInstructionTotal) * 100.0D;
	}

	public boolean addTestCase(TestCaseJSON testCaseJSON) {
		if (this.testCases == null) {
			this.testCases = new ArrayList<>();
		}
		return this.testCases.add(testCaseJSON);
	}
}
