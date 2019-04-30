package eu.stamp_project.dspot.selector;

import java.text.DecimalFormat;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/07/17
 */
public class JacocoCoverageSelectorTest extends AbstractSelectorRemoveDuplicationTest {

	public static final String nl = System.getProperty("line.separator");

	private static final char DECIMAL_SEPARATOR = (((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator());

	@Override
	protected TestSelector getTestSelector() {
		return new JacocoCoverageSelector();
	}

	@Override
	protected String getPathToReportFileDuplication() {
		return "target/trash/example.TestSuiteDuplicationExample_jacoco_instr_coverage_report.txt";
	}

	@Override
	protected String getContentReportFileDuplication() {
		return nl + "======= REPORT =======" + nl +
				"Initial instruction coverage: 23 / 38" + nl +
				"60" + DECIMAL_SEPARATOR + "53%" + nl +
				"Amplification results with 3 amplified tests." + nl +
				"Amplified instruction coverage: 31 / 38" + nl +
				"81" + DECIMAL_SEPARATOR + "58%";
	}
}
