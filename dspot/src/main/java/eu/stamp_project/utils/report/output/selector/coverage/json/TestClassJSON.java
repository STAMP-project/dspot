package eu.stamp_project.utils.report.output.selector.coverage.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/07/17
 */
public class TestClassJSON implements eu.stamp_project.utils.report.output.selector.TestClassJSON {

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

	public String toString() {
		JSONObject subJson = new JSONObject()
                  .put("initialCoverage", this.initialInstructionCovered)
                  .put("ampCoverage", this.amplifiedInstructionCovered)
                  .put("totalCoverage", this.amplifiedInstructionTotal);

        String jsonString = new JSONObject().put(this.name,subJson).toString();
        return jsonString;
	}
}
