package eu.stamp_project.dspot.selector;

import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.utils.report.output.selector.TestSelectorReport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/17
 */
public class TakeAllSelector implements TestSelector {

	protected List<CtMethod<?>> selectedAmplifiedTest;

	protected InputConfiguration configuration;

	protected CtType<?> currentClassTestToBeAmplified;

	public TakeAllSelector() {
		this.selectedAmplifiedTest = new ArrayList<>();
	}

	@Override
	public void init(InputConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
		if (this.currentClassTestToBeAmplified == null) {
			this.currentClassTestToBeAmplified = classTest;
			this.selectedAmplifiedTest.clear();
		}
		return testsToBeAmplified;
	}

	@Override
	public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
		this.selectedAmplifiedTest.addAll(amplifiedTestToBeKept);
		return amplifiedTestToBeKept;
	}

	@Override
	public TestSelectorElementReport report() {
		final String report = "Amplification results with " + this.selectedAmplifiedTest.size() + " new tests.";
		reset();
		return new TestSelectorElementReportImpl(report, null);
	}

	@Override
	public List<CtMethod<?>> getAmplifiedTestCases() {
		return this.selectedAmplifiedTest;
	}

	protected void reset() {
		this.currentClassTestToBeAmplified = null;
	}

}
