package eu.stamp_project.mutant.pit;

import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PitCSVParserAndResultTest {

    @Test
    public void test() throws Exception {
        final List<AbstractPitResult> pitCSVResults = (new PitCSVResultParser()).parse(new File("src/test/resources/mutations_spoon.csv"));
        long nbErrors = pitCSVResults.stream()
                .filter(pitResult ->
                        pitResult.getStateOfMutant() == PitCSVResult.State.MEMORY_ERROR ||
                                pitResult.getStateOfMutant() == PitCSVResult.State.NON_VIABLE ||
                                pitResult.getStateOfMutant() == PitCSVResult.State.TIMED_OUT
                ).count();
        assertEquals(283, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.SURVIVED).count(), nbErrors);
        assertEquals(3343, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.KILLED).count(), nbErrors);
        assertEquals(1014, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.NO_COVERAGE).count(), nbErrors);
    }
}
