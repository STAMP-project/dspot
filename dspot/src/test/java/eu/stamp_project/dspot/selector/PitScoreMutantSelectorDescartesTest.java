package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.NumberLiteralAmplifier;
import eu.stamp_project.dspot.amplifier.StringLiteralAmplifier;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.RandomHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class PitScoreMutantSelectorDescartesTest {

    @Before
    public void setUp() throws Exception {
        RandomHelper.setSeedRandom(23L);
        try {
            FileUtils.deleteDirectory(new File("target/dspot"));
        } catch (Exception ignored) {

        }
        try {
        FileUtils.deleteDirectory(new File("src/test/resources/test-projects/target/"));
        } catch (Exception ignored) {

        }
        Utils.getInputConfiguration().setVerbose(true);
        Utils.getInputConfiguration().setDescartesMode(false);
    }

    @Ignore
    @Test
    public void testPitDescartesMode() throws Exception {

        /*
            weak contract: this test should not throw any exception and end properly
                the increase of the mutation score and the selection is delegated to dedicated test
                here we test that the descartes mode runs
         */

        assertFalse(Utils.getInputConfiguration().isDescartesMode());
        Utils.getInputConfiguration().setDescartesMode(true);
        InputConfiguration configuration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        DSpot dspot = new DSpot(configuration, 1,
                Arrays.asList(new StringLiteralAmplifier(), new NumberLiteralAmplifier()),
                new PitMutantScoreSelector());
        dspot.amplifyTest("example.TestSuiteExample", Collections.singletonList("test2"));
        FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        assertTrue(Utils.getInputConfiguration().isDescartesMode());
    }

    @After
    public void tearDown() throws Exception {
        Utils.getInputConfiguration().setPitVersion("1.3.0");
        Utils.getInputConfiguration().setDescartesMode(false);
    }
}

