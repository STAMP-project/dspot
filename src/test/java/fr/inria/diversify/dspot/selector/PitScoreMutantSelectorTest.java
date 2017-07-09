package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.Utils;
import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.MavenAutomaticBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.MavenAbstractTest;
import fr.inria.diversify.dspot.amplifier.StatementAdderOnAssert;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.PrintClassUtils;
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
        DSpot dspot = new DSpot(configuration, 1, Collections.singletonList(new StatementAdderOnAssert()),
                new PitMutantScoreSelector("src/test/resources/test-projects/originalpit/mutations.csv"));//loading from existing pit-results

        final CtClass<Object> exampleOriginalTestClass = dspot.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        CtType amplifiedTest = dspot.amplifyAllTests().get(0);

        assertTrue(amplifiedTest.getMethods().size() > exampleOriginalTestClass.getMethods().size());

        File directory = new File(dspot.getInputProgram().getProgramDir() + "/" + dspot.getInputProgram().getRelativeTestSourceCodeDir());
        PrintClassUtils.printJavaFile(directory, amplifiedTest);

        AutomaticBuilder builder = new MavenAutomaticBuilder(Utils.getInputConfiguration());
        List<PitResult> pitResultsAmplified = builder.runPit(Utils.getInputProgram().getProgramDir(),
                amplifiedTest);

        assertTrue(null != pitResultsAmplified);
        assertTrue(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count() <
                pitResultsAmplified.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());

        PrintClassUtils.printJavaFile(directory, exampleOriginalTestClass);
    }

}
