package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.StringLiteralAmplifier;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/03/18
 */
public abstract class AbstractSelectorRemoveOverlapTest {

    protected String getPathToProperties() {
        return "src/test/resources/regression/test-projects_2/test-projects.properties";
    }

    protected abstract TestSelector getTestSelector();

    protected abstract String getContentReportFile();

    protected TestSelector testSelectorUnderTest;

    protected CtClass<?> getTestClass() {
        return Utils.findClass("example.TestSuiteOverlapExample");
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
        this.testSelectorUnderTest.init();
        DSpot dspot = new DSpot(
                new TestFinder(Collections.emptyList(), Collections.emptyList()),
                Utils.getCompiler(),
                this.testSelectorUnderTest,
                InputConfiguration.get().getBudgetizer().getInputAmplDistributor(new StringLiteralAmplifier()),
                new Output(InputConfiguration.get().getAbsolutePathToProjectRoot(), InputConfiguration.get().getOutputDirectory()),
                1,
                InputConfiguration.get().shouldGenerateAmplifiedTestClass());
        dspot.amplify(Utils.findClass("example.TestSuiteOverlapExample"), Collections.emptyList());
        final File directory = new File(DSpotUtils.shouldAddSeparator.apply(InputConfiguration.get().getOutputDirectory()));
        if (!directory.exists()) {
            directory.mkdir();
        }
        assertEquals(getContentReportFile(), this.testSelectorUnderTest.report().output(this.getTestClass()));
    }
}
