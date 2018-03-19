package fr.inria.diversify.dspot.selector;

import fr.inria.Utils;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.minimization.GeneralMinimizer;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.stream.Collectors;

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
						Collections.singletonList(getTest())
				)
		);
		assertFalse(this.testSelectorUnderTest.getAmplifiedTestCases().isEmpty());

		this.testSelectorUnderTest.selectToKeep(
				this.testSelectorUnderTest.selectToAmplify(
						Collections.singletonList(getAmplifiedTest())
				)
		);
		assertFalse(this.testSelectorUnderTest.getAmplifiedTestCases().isEmpty());
		this.testSelectorUnderTest.report();
	}
}
