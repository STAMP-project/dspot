package eu.stamp_project.utils.execution;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.testrunner.listener.pit.AbstractPitResult;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/04/19
 */
public class PitRunnerTest extends AbstractTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        InputConfiguration.get().getBuilder().reset();
        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.sample.*");
        InputConfiguration.get().setUseAutomaticBuilder(false);
        InputConfiguration.get().setJUnit5(false);
        InputConfiguration.get().setDescartesMode(true);
    }

    @Test
    public void testRunPitAPI() {

        /*
            Test execution of pit using API
         */



        // DESCARTES MODE (default)

        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        List<? extends AbstractPitResult> pitResults =
                new PitRunner().runPit(Utils.findClass("fr.inria.pit.TestClass"));
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // RUN ALL THE TEST

        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        pitResults = new PitRunner().runPit();
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // GREGOR MODE (with ALL mutators)

        InputConfiguration.get().setDescartesMode(false);
        pitResults = new PitRunner().runPit(Utils.findClass("fr.inria.pit.TestClass"));
        assertEquals(24, pitResults.size());
        assertEquals(19, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(5, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
    }

    @Test
    public void testRunPitAPIJUnit5() {

        /*
            Test execution of pit using API
         */



        // DESCARTES MODE (default)

        InputConfiguration.get().setJUnit5(true);
        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        List<? extends AbstractPitResult> pitResults = new PitRunner().runPit(Utils.findClass("fr.inria.pit.junit5.TestClass"));
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // RUN ALL THE TEST

        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        pitResults = new PitRunner().runPit();
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // GREGOR MODE (with ALL mutators)

        InputConfiguration.get().setDescartesMode(false);
        pitResults = new PitRunner().runPit(Utils.findClass("fr.inria.pit.junit5.TestClass"));
        assertEquals(24, pitResults.size());
        assertEquals(19, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(5, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
    }

    @Test
    public void testRunPitAutomaticBuilder() {

        /*
            Test execution of pit using maven
         */

        // DESCARTES MODE (default)

        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        InputConfiguration.get().setUseAutomaticBuilder(true);
        List<? extends AbstractPitResult> pitResults =
                new PitRunner().runPit(Utils.findClass("fr.inria.pit.TestClass"));
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // RUN ALL THE TEST

        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        pitResults = new PitRunner().runPit();
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // GREGOR MODE (with ALL mutators)

        InputConfiguration.get().getBuilder().reset();
        InputConfiguration.get().setDescartesMode(false);
        pitResults = new PitRunner().runPit(Utils.findClass("fr.inria.pit.TestClass"));
        assertEquals(31, pitResults.size());
        assertEquals(24, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(7, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
    }

    @Ignore
    @Test
    public void testRunPitAutomaticBuilderJUnit5() {

        /*
            Test execution of pit using maven on junit 5
                TODO
                Cannot be run, it seems that the sample project is not well formed
                the mixin of junit4 and junit5 tests are breaking the maven execution

         */

        // DESCARTES MODE (default)

        InputConfiguration.get().setJUnit5(true);
        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        InputConfiguration.get().setUseAutomaticBuilder(true);
        InputConfiguration.get().getBuilder().reset();
        List<? extends AbstractPitResult> pitResults =
                new PitRunner().runPit(Utils.findClass("fr.inria.pit.TestClass"));
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // RUN ALL THE TEST

        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");
        pitResults = new PitRunner().runPit();
        assertEquals(1, pitResults.size());
        assertEquals(1, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());

        // GREGOR MODE (with ALL mutators)

        InputConfiguration.get().getBuilder().reset();
        InputConfiguration.get().setDescartesMode(false);
        pitResults = new PitRunner().runPit(Utils.findClass("fr.inria.pit.TestClass"));
        assertEquals(31, pitResults.size());
        assertEquals(24, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
        assertEquals(7, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
    }

}
