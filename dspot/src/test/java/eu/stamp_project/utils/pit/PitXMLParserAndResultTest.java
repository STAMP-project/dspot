package eu.stamp_project.utils.pit;

import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

    @Test
    public void testOnPitResultCSV() throws IOException {


        /*
                reading, output and re-read should give the save as the first read
                In this test, we use the toString() method of PitCSVResult to transform the
                 List<PitCSVResult> into List<String>
                Then we create a TestSelectorElementReportImpl (we do not care of the textual report and the test class JSON)
                Then the file read (raw) and the string built must be equals
        */

        final PitXMLResultParser parser = new PitXMLResultParser();
        final String FILE_PATH_NAME = "src/test/resources/mutations_test-projects.xml";
        final List<PitXMLResult> parse = parser.parse(new File(FILE_PATH_NAME));
        // transform the PitCSVResult into String using overridden toString() method
        final String resultAsString = parse.stream().map(Object::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
        try (BufferedReader buffer = new BufferedReader(new FileReader(FILE_PATH_NAME))) {
            final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + AmplificationHelper.LINE_SEPARATOR +
                    "<mutations>" + AmplificationHelper.LINE_SEPARATOR;
            final String footer = "</mutations>";
            assertEquals(
                    buffer.lines()
                            .collect(
                                    Collectors.joining(AmplificationHelper.LINE_SEPARATOR)
                            ),
                    header +
                    resultAsString + AmplificationHelper.LINE_SEPARATOR +
                            footer
            );
        }
    }
}
