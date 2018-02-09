package fr.inria.stamp.test.filter;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/07/17
 */
@Deprecated
public class MethodFilter extends Filter {

	private Collection<String> testMethodNames;

	public MethodFilter(Collection<String> testMethodNames) {
		this.testMethodNames = testMethodNames;
	}

	@Override
	public boolean shouldRun(Description description) {
		return this.testMethodNames.isEmpty() || (description.isTest() &&
				testMethodNames.stream().anyMatch(testMethodName ->
						Pattern.compile(testMethodName + "\\[\\d:(.*?)\\]").matcher(description.getMethodName()).find() ||
						Pattern.compile(testMethodName + "\\[\\d\\]").matcher(description.getMethodName()).find()
				) || testMethodNames.contains(description.getMethodName())
				) ||
				description.getChildren().stream()
						.map(this::shouldRun)
						.reduce(Boolean.FALSE, Boolean::logicalOr);
	}

	@Override
	public String describe() {
		return "stamp.fr.inria.filter with name of test method";
	}
}