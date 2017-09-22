package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.MavenAbstractTest;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.sosiefier.runner.InputConfiguration;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.stamp.Main;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/9/17
 */
public class PitScoreMutantSelectorTest extends MavenAbstractTest {

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }

    @Test
    public void testSelection() throws Exception {
        AmplificationHelper.setSeedRandom(23L);

        Main.verbose = true;

        MavenPitCommandAndOptions.evosuiteMode = false;
        MavenPitCommandAndOptions.descartesMode = false;

        List<PitResult> pitResults = PitResultParser.parse(new File("src/test/resources/test-projects/originalpit/mutations.csv"));
        assertTrue(null != pitResults);

        /*
            Now Run DSpot and prove that the amplification allow to kill more mutants.
         */

        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        DSpot dspot = new DSpot(configuration, 1, Collections.emptyList(),
                new PitMutantScoreSelector("src/test/resources/test-projects/originalpit/mutations.csv"));//loading from existing pit-results

        dspot.addAmplifier(new TestDataMutator());

        final CtClass<Object> exampleOriginalTestClass = dspot.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample", Collections.singletonList("test2"));

        assertTrue(amplifiedTest.getMethods().size() > exampleOriginalTestClass.getMethods().size());

        DSpotUtils.printJavaFileWithComment(amplifiedTest, new File(DSpotCompiler.pathToTmpTestSources));
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(configuration)
                .buildClasspath(dspot.getInputProgram().getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                dspot.getInputProgram().getProgramDir() + "/" + dspot.getInputProgram().getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/";

        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(dspot.getInputProgram().getProgramDir() + "/" + dspot.getInputProgram().getTestClassesDir()));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
        List<PitResult> pitResultsAmplified = builder.runPit(configuration.getInputProgram().getProgramDir(),
                amplifiedTest);

        assertTrue(null != pitResultsAmplified);
        assertTrue(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count() <
                pitResultsAmplified.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
    }

}
