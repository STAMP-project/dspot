package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.options.InputConfiguration;
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

    private OverlapDelegator overlapDelegator = new OverlapDelegator();

    private AmplificationDelegator amplificationDelegator = new AmplificationDelegator();

    private class OverlapDelegator extends AbstractSelectorRemoveOverlapTest {

        @Override
        protected TestSelector getTestSelector() {
            return new PitMutantScoreSelector();
        }

        @Override
        protected String getContentReportFile() {
            return "Test class that has been amplified: example.TestSuiteOverlapExample" + AmplificationHelper.LINE_SEPARATOR +
                    "The original test suite kills 2 mutants" + AmplificationHelper.LINE_SEPARATOR +
                    "The amplification results with 1 new tests" + AmplificationHelper.LINE_SEPARATOR +
                    "it kills 3 more mutants" + AmplificationHelper.LINE_SEPARATOR;
        }
    }

    private class AmplificationDelegator extends AbstractSelectorTest {

        @Override
        public void setUp() throws Exception {
            Utils.reset(); // TODO somewhere, there is some states that is why we need to reset here.
            super.setUp();
            InputConfiguration.get().setDescartesMode(false);
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
        protected String getContentReportFile() {
            return "Test class that has been amplified: example.TestSuiteExample" + AmplificationHelper.LINE_SEPARATOR +
                    "The original test suite kills 15 mutants" + AmplificationHelper.LINE_SEPARATOR +
                    "The amplification results with 1 new tests" + AmplificationHelper.LINE_SEPARATOR +
                    "it kills 1 more mutants" + AmplificationHelper.LINE_SEPARATOR;
        }
    }

    @Test
    public void testSelector() throws Exception {
        amplificationDelegator.setUp();
        amplificationDelegator.testSelector();
    }

    @Test
    public void testRemoveOverlappingTests() throws Exception {
        overlapDelegator.setUp();
        overlapDelegator.testRemoveOverlappingTests();
    }
}
