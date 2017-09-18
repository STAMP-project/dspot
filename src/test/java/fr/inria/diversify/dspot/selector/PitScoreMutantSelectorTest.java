package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.MavenAbstractTest;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.PrintClassUtils;
import fr.inria.stamp.Main;
import org.apache.commons.io.FileUtils;
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
    public void testSelection() throws Exception, InvalidSdkException {
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
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration, 1, Collections.singletonList(new TestDataMutator()),
                new PitMutantScoreSelector("src/test/resources/test-projects/originalpit/mutations.csv"));//loading from existing pit-results

        final CtClass<Object> exampleOriginalTestClass = dspot.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample", Collections.singletonList("test2"));

        assertTrue(amplifiedTest.getMethods().size() > exampleOriginalTestClass.getMethods().size());

        File directory = new File(dspot.getInputProgram().getProgramDir() + "/" + dspot.getInputProgram().getRelativeTestSourceCodeDir());
        PrintClassUtils.printJavaFile(directory, amplifiedTest);

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
        List<PitResult> pitResultsAmplified = builder.runPit(configuration.getInputProgram().getProgramDir(),
                amplifiedTest);

        assertTrue(null != pitResultsAmplified);
        assertTrue(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count() <
                pitResultsAmplified.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());

        FileUtils.forceDelete(new File(directory + "/" + amplifiedTest.getQualifiedName().replaceAll("\\.", "/") + ".java"));
    }

}
