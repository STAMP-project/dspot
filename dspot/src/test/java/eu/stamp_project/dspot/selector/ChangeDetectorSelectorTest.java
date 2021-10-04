package eu.stamp_project.dspot.selector;

import eu.stamp_project.UtilsModifier;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/08/17
 */
@SuppressWarnings("unchecked")
public class ChangeDetectorSelectorTest extends AbstractSelectorTest {

    @Override
    protected TestSelector getTestSelector() {
        this.configuration.setAbsolutePathToSecondVersionProjectRoot(new File("src/test/resources/regression/test-projects_1/").getAbsolutePath() + "/");
        return new ChangeDetectorSelector(this.builder, this.configuration);
    }

    @Override
    protected CtMethod<?> getAmplifiedTest() {
        final CtMethod clone = getTest().clone();
        UtilsModifier.replaceGivenLiteralByNewValue(this.factory, clone, -1);
        return clone;
    }

    @Override
    protected String getContentReportFile() {
        return "1 amplified test fails on the new versions." + AmplificationHelper.LINE_SEPARATOR +
                "example.TestSuiteExample#test2(example.TestSuiteExample): String index out of range: -1java.lang.StringIndexOutOfBoundsException: String index out of range: -1" + AmplificationHelper.LINE_SEPARATOR;}
}
