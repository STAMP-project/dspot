package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.minimization.GeneralMinimizer;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/17
 */
public class TakeAllSelectorTest extends AbstractSelectorTest {

	@Override
	protected TestSelector getTestSelector() {
		return new TakeAllSelector();
	}

	@Override
	protected CtMethod<?> getAmplifiedTest() {
		return getTest();
	}

	@Override
	protected String getPathToReportFile() {
		return "";
	}

	@Override
	protected String getContentReportFile() {
		return "";
	}

	@Override
	protected Class<?> getClassMinimizer() {
		return GeneralMinimizer.class;
	}

	@Test
	public void testSelector() throws Exception {
		this.testSelectorUnderTest.init(Utils.getInputConfiguration());
		this.testSelectorUnderTest.selectToKeep(
				this.testSelectorUnderTest.selectToAmplify(
						getTestClass(), Collections.singletonList(getTest())
				)
		);
		assertFalse(this.testSelectorUnderTest.getAmplifiedTestCases().isEmpty());

		this.testSelectorUnderTest.selectToKeep(
				this.testSelectorUnderTest.selectToAmplify(
						getTestClass(), Collections.singletonList(getAmplifiedTest())
				)
		);
		assertFalse(this.testSelectorUnderTest.getAmplifiedTestCases().isEmpty());
		this.testSelectorUnderTest.report();
	}
}
