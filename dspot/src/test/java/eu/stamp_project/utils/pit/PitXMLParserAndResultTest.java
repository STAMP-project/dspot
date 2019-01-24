package eu.stamp_project.utils.pit;

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
        final List<? extends AbstractPitResult> pitXMLResults = (new PitXMLResultParser()).parse(new File("src/test/resources/mutations_test-projects.xml"));
        long nbErrors = pitXMLResults.stream()
                .filter(pitResult ->
                        pitResult.getStateOfMutant() == AbstractPitResult.State.MEMORY_ERROR ||
                                pitResult.getStateOfMutant() == AbstractPitResult.State.NON_VIABLE||
                                pitResult.getStateOfMutant() == AbstractPitResult.State.TIMED_OUT
                ).count();
        assertEquals(9, pitXMLResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count(), nbErrors);
        assertEquals(15, pitXMLResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count(), nbErrors);
        assertEquals(4, pitXMLResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.NO_COVERAGE).count(), nbErrors);
    }
}
