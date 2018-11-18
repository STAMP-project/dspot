package eu.stamp_project.mutant.pit;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 14/11/18
 */
public class PitXMLParserAndResultTest {
    @Test
    public void test() throws Exception {
        final List<PitResult> pitXMLResults = PitXMLParser.parse(new File("src/test/resources/mutations_spoon.csv"));
        long nbErrors = pitXMLResults.stream()
                .filter(pitResult ->
                        pitResult.getStateOfMutant() == PitResult.State.MEMORY_ERROR ||
                                pitResult.getStateOfMutant() == PitResult.State.NON_VIABLE||
                                pitResult.getStateOfMutant() == PitResult.State.TIMED_OUT
                ).count();
        assertEquals(283, pitXMLResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count(), nbErrors);
        assertEquals(3343, pitXMLResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count(), nbErrors);
        assertEquals(1014, pitXMLResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count(), nbErrors);
    }
}
