package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationHelper;
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
    protected String getContentReportFile() {
        return "Test class that has been amplified: example.TestSuiteExample" + AmplificationHelper.LINE_SEPARATOR +
                "The original test suite kills 45 mutants" + AmplificationHelper.LINE_SEPARATOR +
                "The amplification results with 1 new tests" + AmplificationHelper.LINE_SEPARATOR +
                "it kills 2 more mutants" + AmplificationHelper.LINE_SEPARATOR;
    }

}
