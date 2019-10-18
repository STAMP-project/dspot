package eu.stamp_project.utils.pit;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReportImpl;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PitCSVParserAndResultTest {

    @Test
    public void test() throws Exception {
        final List<? extends AbstractPitResult> pitCSVResults = (new PitCSVResultParser()).parse(new File("src/test/resources/mutations_spoon.csv"));
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
