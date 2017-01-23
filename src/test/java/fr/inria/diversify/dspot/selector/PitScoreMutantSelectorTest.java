package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.MavenAbstractTest;
import fr.inria.diversify.dspot.amplifier.StatementAdderOnAssert;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitRunner;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Arrays;
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

        //Utils.reset();

        List<PitResult> pitResults = PitRunner.run(Utils.getInputProgram(), Utils.getInputConfiguration(), Utils.getFactory().Class().get("example.TestSuiteExample"));
        assertTrue(null != pitResults);

        /*
            Now Run DSpot and prove that the amplification allow to kill more mutants.
         */

        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration, 1, Arrays.asList(new StatementAdderOnAssert()), new PitMutantScoreSelector());

        final CtClass<Object> exampleOriginalTestClass = dspot.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        CtType amplifiedTest = dspot.amplifyAllTests().get(0);

        File directory = new File(dspot.getInputProgram().getProgramDir() + "/" + dspot.getInputProgram().getRelativeTestSourceCodeDir());
        PrintClassUtils.printJavaFile(directory, amplifiedTest);

        List<PitResult> pitResultsAmplified = PitRunner.run(Utils.getInputProgram(), Utils.getInputConfiguration(), amplifiedTest);

        assertTrue(null != pitResultsAmplified);
        assertTrue(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count() <
                pitResultsAmplified.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());

        PrintClassUtils.printJavaFile(directory, exampleOriginalTestClass);
    }

}
