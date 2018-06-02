package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.TestDataMutator;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.mutant.pit.PitResult;
import eu.stamp_project.mutant.pit.PitResultParser;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.Main;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/12/17
 */
@Deprecated
public class ExecutedMutantSelectorTest {

    @Before
    public void setUp() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");
    }

    @Ignore
    @Test
    public void test() throws Exception {

        // pre computing the number of executed mutants...
        Main.verbose = true;
        AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration())
                .runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot());
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + "target/pit-reports/");

        final ExecutedMutantSelector testSelector = new ExecutedMutantSelector();
        DSpot dspot = new DSpot(Utils.getInputConfiguration(), 1, Collections.singletonList(new TestDataMutator()), testSelector);
        final CtType amplifyTest = dspot.amplifyTest(Utils.findClass("example.TestSuiteExample"),
                Collections.singletonList(Utils.findMethod("example.TestSuiteExample", "test8")));

        // pretty print it
        DSpotUtils.printCtTypeToGivenDirectory(amplifyTest, new File(DSpotCompiler.pathToTmpTestSources));

        // then compile
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(Utils.getInputConfiguration())
                .buildClasspath()
                + AmplificationHelper.PATH_SEPARATOR +
                Utils.getInputConfiguration().getClasspathClassesProject()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/";

        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(Utils.getInputConfiguration().getAbsolutePathToTestClasses()));

        AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration())
                .runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot());
        final List<PitResult> amplifiedPitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + "target/pit-reports/");

        assertTrue(pitResults.stream()
                .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                        pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count() <
                amplifiedPitResults.stream()
                        .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                                pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        Main.verbose = false;
    }
}
