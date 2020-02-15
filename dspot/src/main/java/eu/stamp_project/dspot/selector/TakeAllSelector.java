package eu.stamp_project.dspot.selector;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReportImpl;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/17
 */
public class TakeAllSelector extends AbstractTestSelector {

	protected List<CtMethod<?>> selectedAmplifiedTest;

	protected CtType<?> currentClassTestToBeAmplified;

	public TakeAllSelector(AutomaticBuilder automaticBuilder, UserInput configuration) {
		super(automaticBuilder, configuration);
		this.selectedAmplifiedTest = new ArrayList<>();
	}

	@Override
	public boolean init() {
		return true;
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
		return new TestSelectorElementReportImpl(report, null, Collections.emptyList(), "");
	}

	@Override
	public List<CtMethod<?>> getAmplifiedTestCases() {
		return this.selectedAmplifiedTest;
	}

	protected void reset() {
		this.currentClassTestToBeAmplified = null;
	}

}
