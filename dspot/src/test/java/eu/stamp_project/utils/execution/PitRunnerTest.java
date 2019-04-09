package eu.stamp_project.utils.execution;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.pit.AbstractPitResult;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/04/19
 */
public class PitRunnerTest {

    @BeforeEach
    void setUp() {
        InputConfiguration.initialize("src/test/resources/sample/sample.properties");
        EntryPoint.setMutationEngine(ConstantsHelper.MutationEngine.DESCARTES);
    }

    @Test
    void testRunPitAPI() {

        /*
            Test execution of pit using API
         */



        // DESCARTES MODE (default)

        List<? extends AbstractPitResult> pitResults =
                //PitRunner.runPit(InputConfiguration.get().getFilter());
                PitRunner.runPit("fr.inria.testframework.TestSupportJUnit5");
        assertEquals(2, pitResults.size());
        assertEquals(2, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // GREGOR MODE (with ALL mutators)

        EntryPoint.setMutationEngine(ConstantsHelper.MutationEngine.GREGOR);
        pitResults =
                //PitRunner.runPit(InputConfiguration.get().getFilter());
                PitRunner.runPit("fr.inria.testframework.TestSupportJUnit5");
        System.out.println(pitResults.size());
        System.out.println(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        System.out.println(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
    }

    @Test
    void testRunPitAPIJUnit5() {

        /*
            Test execution of pit using API
         */



        // DESCARTES MODE (default)

        List<? extends AbstractPitResult> pitResults =
                PitRunner.runPit(InputConfiguration.get().getFilter());
        assertEquals(2, pitResults.size());
        assertEquals(2, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // GREGOR MODE (with ALL mutators)

        EntryPoint.setMutationEngine(ConstantsHelper.MutationEngine.GREGOR);
        pitResults = PitRunner.runPit(InputConfiguration.get().getFilter());
        System.out.println(pitResults.size());
        System.out.println(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        System.out.println(pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
    }

    @Test
    void testRunPitAutomaticBuilder() {

        /*
            Test execution of pit using API
         */

    }

}
