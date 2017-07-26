package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.Utils;
import fr.inria.diversify.automaticbuilder.MavenAutomaticBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/07/17
 */
public class JacocoCoverageSelectorTest {

	@Test
	public void testJaCoCoCoverageSelector() throws Exception, InvalidSdkException {
		Utils.init("src/test/resources/test-projects/test-projects.properties");
		final JacocoCoverageSelector jacocoCoverageSelector = new JacocoCoverageSelector();
		jacocoCoverageSelector.init(Utils.getInputConfiguration());

		List<CtMethod<?>> testMethods = new ArrayList<>(Utils.getFactory().Class().get("example.TestSuiteExample").getMethods());
		assertEquals(testMethods, jacocoCoverageSelector.selectToAmplify(testMethods));

		AmplificationHelper.setSeedRandom(23L);
		DSpot dspot = new DSpot(Utils.getInputConfiguration());

		CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");

		Utils.reset();
		Utils.init("src/test/resources/test-projects/test-projects.properties");

		MavenAutomaticBuilder builder = new MavenAutomaticBuilder(Utils.getInputConfiguration());
		String dependencies = builder.buildClasspath(Utils.getInputProgram().getProgramDir());
		DSpotCompiler.compile(Utils.getInputConfiguration().getOutputDirectory(),
				Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getClassesDir() + ":" + dependencies,
				new File(Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir())); // TODO

		final List<CtMethod<?>> amplifiedTests = ((Set<CtMethod<?>>) amplifiedTest.getMethods()).stream()
				.filter(ctMethod ->
						ctMethod.getSimpleName().contains("_")
				).collect(Collectors.toList());

		final List<CtMethod<?>> ctMethods = jacocoCoverageSelector.selectToKeep(amplifiedTests);

		assertEquals(12, ctMethods.size());
	}
}
