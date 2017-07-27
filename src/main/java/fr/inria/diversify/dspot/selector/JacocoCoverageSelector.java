package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.coverage.CoverageResults;
import fr.inria.stamp.coverage.JacocoExecutor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class JacocoCoverageSelector implements TestSelector {

	private InputProgram program;

	private CtType<?> currentClassTestToBeAmplified;

	private List<CtMethod<?>> selectedAmplifiedTest = new ArrayList<>();

	private Map<String, CoverageResults> selectedToBeAmplifiedCoverageResultsMap;

	@Override
	public void init(InputConfiguration configuration) {
		this.program = configuration.getInputProgram();
		this.selectedAmplifiedTest.clear();
	}

	@Override
	public void reset() {
		//empty
	}

	@Override
	public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
		if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
			this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();

		}
		final List<String> methodNames = testsToBeAmplified.stream().map(CtNamedElement::getSimpleName).collect(Collectors.toList());
		this.selectedToBeAmplifiedCoverageResultsMap = new JacocoExecutor(this.program).executeJacoco(
				this.currentClassTestToBeAmplified.getQualifiedName(), methodNames);
		return testsToBeAmplified;
	}

	@Override
	public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
		final List<String> methodNames = amplifiedTestToBeKept.stream().map(CtNamedElement::getSimpleName).collect(Collectors.toList());
		final Map<String, CoverageResults> coverageResultsMap = new JacocoExecutor(this.program).executeJacoco(
				this.currentClassTestToBeAmplified.getQualifiedName(), methodNames);
		final List<CtMethod<?>> methodsKept = amplifiedTestToBeKept.stream()
				.filter(ctMethod ->
						coverageResultsMap.get(ctMethod.getSimpleName()).isBetterThan(
								this.selectedToBeAmplifiedCoverageResultsMap.get(
										getFirstParentThatHasBeenRun(ctMethod).getSimpleName())
						)
				).collect(Collectors.toList());
		this.selectedAmplifiedTest = new ArrayList<>(methodsKept);
		return methodsKept;
	}

	private CtMethod<?> getFirstParentThatHasBeenRun(CtMethod<?> test) {
		CtMethod<?> currentParent = AmplificationHelper.getAmpTestToParent().get(test);
		while (AmplificationHelper.getAmpTestToParent().get(currentParent) != null) {
			if (this.selectedToBeAmplifiedCoverageResultsMap.get(currentParent.getSimpleName()) != null) {
				return currentParent;
			} else {
				currentParent = AmplificationHelper.getAmpTestToParent().get(currentParent);
			}
		}
		return currentParent;
	}

	@Override
	public void update() {
		//empty
	}

	@Override
	public void report() {
		System.out.println(this.selectedAmplifiedTest);
	}

	@Override //TODO factorize with pit selector
	public int getNbAmplifiedTestCase() {
		return this.selectedAmplifiedTest.size();
	}

	@Override //TODO factorize with pit selector
	public CtType buildClassForSelection(CtType original, List<CtMethod<?>> methods) {
		CtType clone = original.clone();
		original.getPackage().addType(clone);
		methods.forEach(clone::addMethod);
		return clone;
	}
}
