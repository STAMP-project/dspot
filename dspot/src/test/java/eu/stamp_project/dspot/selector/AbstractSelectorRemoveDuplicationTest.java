package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.StringLiteralAmplifier;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Before;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/03/18
 */
public abstract class AbstractSelectorRemoveDuplicationTest {

    protected String getPathToProperties() {
        return "src/test/resources/regression/test-projects_2/test-projects.properties";
    }

    protected abstract TestSelector getTestSelector();

    protected abstract String getPathToReportFileDuplication();

    protected abstract String getContentReportFileDuplication();

    protected TestSelector testSelectorUnderTest;

    @Before
    public void setUp() throws Exception {
        Utils.reset();
        final String configurationPath = getPathToProperties();
        Utils.init(configurationPath);
        this.testSelectorUnderTest = this.getTestSelector();
    }

    @Test
    public void testRemoveOverlappingTests() throws Exception {
        DSpot dspot = new DSpot(1, Arrays.asList(new StringLiteralAmplifier()), testSelectorUnderTest);
        dspot.amplifyTestClass("example.TestSuiteDuplicationExample");
        try (BufferedReader buffer = new BufferedReader(new FileReader(getPathToReportFileDuplication()))) {
            assertEquals(getContentReportFileDuplication(),
                    buffer.lines()
                            .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
