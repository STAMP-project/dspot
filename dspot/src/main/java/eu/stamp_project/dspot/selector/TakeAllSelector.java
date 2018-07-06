package eu.stamp_project.dspot.selector;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.minimization.GeneralMinimizer;
import eu.stamp_project.minimization.Minimizer;
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
	public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
		if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
			this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
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
	public void report() {
		System.out.println("Amplification results with " + this.selectedAmplifiedTest.size() + " new tests.");
		reset();
	}

	@Override
	public List<CtMethod<?>> getAmplifiedTestCases() {
		return this.selectedAmplifiedTest;
	}

	protected void reset() {
		this.currentClassTestToBeAmplified = null;
	}

	@Override
	public Minimizer getMinimizer() {
		return new GeneralMinimizer();
	}
}
