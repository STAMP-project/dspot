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
public class PitParserAndResultTest {

    @Test
    public void test() throws Exception {
        final List<PitResult> pitResults = PitResultParser.parse(new File("src/test/resources/mutations_spoon.csv"));
        long nbErrors = pitResults.stream()
                .filter(pitResult ->
                        pitResult.getStateOfMutant() == PitResult.State.MEMORY_ERROR ||
                                pitResult.getStateOfMutant() == PitResult.State.NON_VIABLE||
                                pitResult.getStateOfMutant() == PitResult.State.TIMED_OUT
                ).count();
        assertEquals(283, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count(), nbErrors);
        assertEquals(3343, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count(), nbErrors);
        assertEquals(1014, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count(), nbErrors);
    }
}