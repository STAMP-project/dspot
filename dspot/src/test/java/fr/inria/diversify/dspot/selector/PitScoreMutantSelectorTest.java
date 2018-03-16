package fr.inria.diversify.dspot.selector;

import fr.inria.Utils;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/9/17
 */
public class PitScoreMutantSelectorTest extends AbstractSelectorTest {

      /*
            Test the PitMutantScoreSelector:
                - The amplified test should increase the mutation score of the test suite.
                    we compare the mutation score before and after.
         */

    @Override
    protected TestSelector getTestSelector() {
        return new PitMutantScoreSelector();
    }

    @Override
    protected CtMethod<?> getAmplifiedTest() {
        final CtMethod<?> clone = getTest().clone();
        Utils.replaceGivenLiteralByNewValue(clone, 4);
        return clone;
    }

    @Override
    protected String getPathToReportFile() {
        return "target/trash/example.TestSuiteExample_mutants_report.txt";
    }

    @Override
    protected String getContentReportFile() {
        return "======= REPORT =======" + AmplificationHelper.LINE_SEPARATOR +
                "PitMutantScoreSelector: " + AmplificationHelper.LINE_SEPARATOR +
                "The original test suite kills 13 mutants" + AmplificationHelper.LINE_SEPARATOR +
                "The amplification results with 4 new tests" + AmplificationHelper.LINE_SEPARATOR +
                "it kills 7 more mutants" + AmplificationHelper.LINE_SEPARATOR;
    }
}
