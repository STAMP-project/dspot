package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.minimization.GeneralMinimizer;
import org.junit.Before;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/12/17
 *
 * This Selector has been disabled and should be remove soon
 *
 */
@SuppressWarnings("unchecked")
public class CloverCoverageSelectorTest { //extends AbstractSelectorTest {
    /*
    @Override
    @Before
    public void setUp() throws Exception {
        Utils.reset();
        super.setUp();
    }

    @Override
    protected TestSelector getTestSelector() {
        return new CloverCoverageSelector();
    }

    @Override
    protected CtMethod<?> getAmplifiedTest() {
        final CtMethod<?> clone = getTest().clone();
        Utils.replaceGivenLiteralByNewValue(clone, 'a');
        Utils.replaceGivenLiteralByNewValue(clone, 0);
        return clone;
    }

    @Override
    protected String getPathToReportFile() {
        return "target/trash/example.TestSuiteExample_clover_coverage.txt";
    }

    @Override
    protected String getContentReportFile() {
        return  "======= REPORT =======" + AmplificationHelper.LINE_SEPARATOR +
                "Initial Coverage: 30 / 34" + AmplificationHelper.LINE_SEPARATOR +
                "The amplification results with: 1 amplified test cases" + AmplificationHelper.LINE_SEPARATOR +
                "Amplified Coverage: 34 / 34";
    }

    @Override
    protected Class<?> getClassMinimizer() {
        return GeneralMinimizer.class;
    }
    */
}
