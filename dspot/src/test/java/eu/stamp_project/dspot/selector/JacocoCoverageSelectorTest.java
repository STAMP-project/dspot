package eu.stamp_project.dspot.selector;

import java.text.DecimalFormat;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/07/17
 */
public class JacocoCoverageSelectorTest extends AbstractSelectorRemoveOverlapTest {

	public static final String nl = System.getProperty("line.separator");

	private static final char DECIMAL_SEPARATOR = (((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator());

	@Override
	protected TestSelector getTestSelector() {
		return new JacocoCoverageSelector(
				this.builder,
				this.configuration
		);
	}

	@Override
	protected String getContentReportFile() {
		return "Initial instruction coverage: 23 / 38" + nl +
				"60" + DECIMAL_SEPARATOR + "53%" + nl +
				"Amplification results with 3 amplified tests." + nl +
				"Amplified instruction coverage: 31 / 38" + nl +
				"81" + DECIMAL_SEPARATOR + "58%" + nl;
	}
}
