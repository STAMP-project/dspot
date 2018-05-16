package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.minimization.PitMutantMinimizer;
import org.junit.Before;
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
    @Before
    public void setUp() throws Exception {
        Utils.reset(); // TODO somewhere, there is some states that is why we need to reset here.
        super.setUp();
    }

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
        return AmplificationHelper.LINE_SEPARATOR +
                "======= REPORT =======" + AmplificationHelper.LINE_SEPARATOR +
                "PitMutantScoreSelector: " + AmplificationHelper.LINE_SEPARATOR +
                "The original test suite kills 15 mutants" + AmplificationHelper.LINE_SEPARATOR +
                "The amplification results with 1 new tests" + AmplificationHelper.LINE_SEPARATOR +
                "it kills 1 more mutants";
    }

    @Override
    protected Class<?> getClassMinimizer() {
        return PitMutantMinimizer.class;
    }
}
