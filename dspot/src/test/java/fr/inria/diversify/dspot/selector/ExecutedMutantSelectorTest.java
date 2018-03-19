package fr.inria.diversify.dspot.selector;

import fr.inria.Utils;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.stamp.Main;
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
                .runPit(Utils.getInputProgram().getProgramDir());
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + "target/pit-reports/");

        final ExecutedMutantSelector testSelector = new ExecutedMutantSelector();
        DSpot dspot = new DSpot(Utils.getInputConfiguration(), 1, Collections.singletonList(new TestDataMutator()), testSelector);
        final CtType amplifyTest = dspot.amplifyTest(Utils.findClass("example.TestSuiteExample"),
                Collections.singletonList(Utils.findMethod("example.TestSuiteExample", "test8")));

        // pretty print it
        DSpotUtils.printCtTypeToGivenDirectory(amplifyTest, new File(DSpotCompiler.pathToTmpTestSources));

        // then compile
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(Utils.getInputConfiguration())
                .buildClasspath(Utils.getInputProgram().getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/"
                + AmplificationHelper.PATH_SEPARATOR +
                Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir();

        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir()));

        AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration())
                .runPit(Utils.getInputProgram().getProgramDir());
        final List<PitResult> amplifiedPitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + "target/pit-reports/");

        assertTrue(pitResults.stream()
                .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                        pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count() <
                amplifiedPitResults.stream()
                        .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                                pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        Main.verbose = false;
    }
}
