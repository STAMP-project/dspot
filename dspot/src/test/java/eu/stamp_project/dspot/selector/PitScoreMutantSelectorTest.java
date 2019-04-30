package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.minimization.PitMutantMinimizer;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/9/17
 */
public class PitScoreMutantSelectorTest {

      /*
            Test the PitMutantScoreSelector:
                - The amplified test should increase the mutation score of the test suite.
                    we compare the mutation score before and after.
         */

    private DuplicationDelegator duplicationDelegator = new DuplicationDelegator();

    private AmplificationDelegator amplificationDelegator = new AmplificationDelegator();

    private class DuplicationDelegator extends AbstractSelectorRemoveDuplicationTest {

        @Override
        protected TestSelector getTestSelector() {
            return new PitMutantScoreSelector();
        }

        @Override
        protected String getPathToReportFileDuplication() {
            return "target/trash/example.TestSuiteDuplicationExample_mutants_report.txt";
        }

        @Override
        protected String getContentReportFileDuplication() {
            return AmplificationHelper.LINE_SEPARATOR +
                    "======= REPORT =======" + AmplificationHelper.LINE_SEPARATOR +
                    "PitMutantScoreSelector: " + AmplificationHelper.LINE_SEPARATOR +
                    "The original test suite kills 2 mutants" + AmplificationHelper.LINE_SEPARATOR +
                    "The amplification results with 1 new tests" + AmplificationHelper.LINE_SEPARATOR +
                    "it kills 3 more mutants";
        }
    }

    private class AmplificationDelegator extends AbstractSelectorTest {

        @Override
        public void setUp() throws Exception {
            Utils.reset(); // TODO somewhere, there is some states that is why we need to reset here.
            super.setUp();
            Utils.getInputConfiguration().setDescartesMode(false);
            DSpotPOMCreator.createNewPom();
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

    @Test
    public void testSelector() throws Exception {
        amplificationDelegator.setUp();
        amplificationDelegator.testSelector();
    }

    @Test
    public void testRemoveOverlappingTests() throws Exception {
        duplicationDelegator.setUp();
        duplicationDelegator.testRemoveOverlappingTests();
    }
}
