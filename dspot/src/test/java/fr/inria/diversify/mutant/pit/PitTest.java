package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.Utils;
import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.*;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PitTest extends MavenAbstractTest {

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }


    //TODO is flaky
    @Test
    public void testPitEvosuiteMode() throws Exception {

        /* by evosuite mode, we mean the common subset of mutation operators between pitest and evosuite */

        AmplificationHelper.setSeedRandom(23L);
        MavenPitCommandAndOptions.descartesMode = false;
        MavenPitCommandAndOptions.evosuiteMode = true;

        Utils.init(this.getPathToPropertiesFile());
        CtClass<Object> testClass = Utils.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration());

        builder.runPit(Utils.getInputProgram().getProgramDir(), testClass);
        List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir()+ builder.getOutputDirectoryPit());

        assertTrue(null != pitResults);

        long nbErrors = pitResults.stream()
                .filter(pitResult ->
                        pitResult.getStateOfMutant() == PitResult.State.MEMORY_ERROR ||
                                pitResult.getStateOfMutant() == PitResult.State.NON_VIABLE||
                                pitResult.getStateOfMutant() == PitResult.State.TIMED_OUT
                ).count();

        assertEquals(8, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count(), nbErrors);
        assertEquals(8, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count(), nbErrors);
        assertEquals(2, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count(), nbErrors);
    }

    @Test
    public void testPit() throws Exception {

        /*
            Run the PitRunner on the test-project example.
                Checks that the PitRunner return well the results, and verify state of mutant.
         */
        AmplificationHelper.setSeedRandom(23L);
        MavenPitCommandAndOptions.descartesMode = false;
        MavenPitCommandAndOptions.evosuiteMode = false;
        Utils.init(this.getPathToPropertiesFile());
        CtClass<Object> testClass = Utils.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration());
        builder.runPit(Utils.getInputProgram().getProgramDir(), testClass);
        List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + builder.getOutputDirectoryPit());

        assertTrue(null != pitResults);

        long nbErrors = pitResults.stream()
                .filter(pitResult ->
                        pitResult.getStateOfMutant() == PitResult.State.MEMORY_ERROR ||
                                pitResult.getStateOfMutant() == PitResult.State.NON_VIABLE||
                                pitResult.getStateOfMutant() == PitResult.State.TIMED_OUT
                ).count();

        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count(), nbErrors);
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count(), nbErrors);
        assertEquals(4, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count(), nbErrors);
    }
}