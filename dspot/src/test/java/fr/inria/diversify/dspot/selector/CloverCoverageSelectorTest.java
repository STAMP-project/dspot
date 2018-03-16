package fr.inria.diversify.dspot.selector;

import fr.inria.Utils;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/12/17
 */
@SuppressWarnings("unchecked")
public class CloverCoverageSelectorTest extends AbstractSelectorTest {

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
        return "target/trash/example.TestSuiteExample_change_report.txt";
    }

    @Override
    protected String getContentReportFile() {
        return  "======= REPORT ======="+ AmplificationHelper.LINE_SEPARATOR +
                "1 amplified test fails on the new versions."+ AmplificationHelper.LINE_SEPARATOR +
                "test2(example.TestSuiteExample): String index out of range: -1";
    }
}
