package eu.stamp_project.dspot;

import eu.stamp_project.Utils;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/05/17
 */
public class DSpotMultiplePomTest {

    @Before
    public void setUp() throws Exception {
        try {
            FileUtils.forceDelete(new File("target/trash/"));
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {
            //ignored
        }
        Utils.init("src/test/resources/multiple-pom/deep-pom-modules.properties");
        Utils.getInputConfiguration().setVerbose(true);
        EntryPoint.verbose = true;
    }

    @Test
    public void testCopyMultipleModuleProject() throws Exception {

        /*
            Contract: DSpot is able to amplify a multi-module project
         */

        final JacocoCoverageSelector testSelector = new JacocoCoverageSelector();
        final TestFinder testFinder = new TestFinder(Collections.emptyList(), Collections.emptyList());
        final List<CtType<?>> testClasses = testFinder.findTestClasses(Collections.singletonList("all"));
        final DSpot dspot = new DSpot(
                testFinder,
                Utils.getCompiler(),
                testSelector,
                InputConfiguration.get().getBudgetizer().getInputAmplDistributor(),
                Output.get(InputConfiguration.get()),
                3);
        final List<CtType<?>> ctTypes = dspot.amplify(testClasses);
        assertFalse(ctTypes.isEmpty());

        Utils.getInputConfiguration().setVerbose(false);
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.forceDelete(new File("target/trash/"));
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {
            //ignored
        }
    }
}
