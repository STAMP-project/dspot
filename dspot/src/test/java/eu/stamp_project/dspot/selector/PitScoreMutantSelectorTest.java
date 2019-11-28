package eu.stamp_project.dspot.selector;

import eu.stamp_project.UtilsModifier;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
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
            return new PitMutantScoreSelector(this.builder, this.configuration);
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
        protected TestSelector getTestSelector() {
            return new PitMutantScoreSelector(this.builder, this.configuration);
        }

        @Override
        protected CtMethod<?> getAmplifiedTest() {
            final CtMethod<?> clone = getTest().clone();
            UtilsModifier.replaceGivenLiteralByNewValue(this.factory, clone, 4);
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
