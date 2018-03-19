package fr.inria.diversify.dspot.selector;

import fr.inria.Utils;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.Main;
import fr.inria.stamp.minimization.ChangeMinimizer;
import fr.inria.stamp.minimization.Minimizer;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/08/17
 */
@SuppressWarnings("unchecked")
public class ChangeDetectorSelectorTest extends AbstractSelectorTest {

    @Override
    protected TestSelector getTestSelector() {
        return new ChangeDetectorSelector();
    }

    @Override
    protected CtMethod<?> getAmplifiedTest() {
        final CtMethod clone = getTest().clone();
        Utils.replaceGivenLiteralByNewValue(clone, -1);
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

    @Override
    protected Class<?> getClassMinimizer() {
        return ChangeMinimizer.class;
    }

    @Test
    public void testOnMultiModuleProject() throws Exception {

        Main.verbose = true;

		/*
            Test that we can use the Change Detector on a multi module project
				The amplification is still done on one single module.
				DSpot should be able to return an amplified test that catch changes.
		 */

        try {
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {

        }

        final String configurationPath = "src/test/resources/multiple-pom/deep-pom-modules.properties";
        Utils.init(configurationPath);
        final ChangeDetectorSelector changeDetectorSelector = new ChangeDetectorSelector();
        changeDetectorSelector.init(Utils.getInputConfiguration());
        assertFalse(changeDetectorSelector.selectToKeep(changeDetectorSelector.selectToAmplify(
                Utils.getAllTestMethodsFrom("fr.inria.multiple.pom.HelloWorldTest"))
        ).isEmpty());

        Main.verbose = false;
    }


}
