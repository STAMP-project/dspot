package eu.stamp_project.dspot.selector;

import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.util.Collections;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/17
 */
public class TakeAllSelectorTest extends AbstractSelectorTest {

	@Override
	protected TestSelector getTestSelector() {
		return new TakeAllSelector(this.builder, this.configuration, this.testRunner);
	}

	@Override
	protected CtMethod<?> getAmplifiedTest() {
		return getTest();
	}

	@Override
	protected String getContentReportFile() {
		return "";
	}

	@Test
	public void testSelector() throws Exception {
		this.testSelectorUnderTest.init();
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
