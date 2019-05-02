package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.StringLiteralAmplifier;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import java.io.File;
import java.util.Arrays;
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

    protected abstract String getContentReportFile();

    protected TestSelector testSelectorUnderTest;

    protected CtClass<?> getTestClass() {
        return Utils.findClass("example.TestSuiteDuplicationExample");
    }

    @Before
    public void setUp() throws Exception {
        Utils.reset();
        final String configurationPath = getPathToProperties();
        Utils.init(configurationPath);
        this.testSelectorUnderTest = this.getTestSelector();
    }

    @Test
    public void testRemoveOverlappingTests() throws Exception {
        this.testSelectorUnderTest.init(Utils.getInputConfiguration());
        DSpot dspot = new DSpot(1, Arrays.asList(new StringLiteralAmplifier()), testSelectorUnderTest);
        dspot.amplifyTestClass("example.TestSuiteDuplicationExample");
        final File directory = new File(DSpotUtils.shouldAddSeparator.apply(InputConfiguration.get().getOutputDirectory()));
        if (!directory.exists()) {
            directory.mkdir();
        }
        assertEquals(getContentReportFile(), this.testSelectorUnderTest.report().output(this.getTestClass()));
    }
}
