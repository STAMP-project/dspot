package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.amplifier.NumberLiteralAmplifier;
import fr.inria.diversify.dspot.amplifier.StringLiteralAmplifier;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.Main;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class PitScoreMutantSelectorDescartesTest {

    @Before
    public void setUp() throws Exception {
        AmplificationHelper.setSeedRandom(23L);
        try {
            FileUtils.deleteDirectory(new File("target/dspot"));
        } catch (Exception ignored) {

        }
        try {
        FileUtils.deleteDirectory(new File("src/test/resources/test-projects/target/"));
        } catch (Exception ignored) {

        }
        Main.verbose = true;
        PitMutantScoreSelector.descartesMode = false;
    }

    @Test
    public void testPitDescartesMode() throws Exception {

        /*
            weak contract: this test should not throw any exception and end properly
                the increase of the mutation score and the selection is delegated to dedicated test
                here we test that the descartes mode runs
         */

        assertFalse(PitMutantScoreSelector.descartesMode);
        PitMutantScoreSelector.descartesMode = true;
        PitMutantScoreSelector.pitVersion = "1.2.0";
        InputConfiguration configuration = new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
        DSpot dspot = new DSpot(configuration, 1,
                Arrays.asList(new StringLiteralAmplifier(), new NumberLiteralAmplifier()),
                new PitMutantScoreSelector());
        dspot.amplifyTest("example.TestSuiteExample", Collections.singletonList("test2"));
        FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        assertTrue(PitMutantScoreSelector.descartesMode);
    }

    @After
    public void tearDown() throws Exception {
        AutomaticBuilderFactory.reset();
        Main.verbose = false;
        PitMutantScoreSelector.pitVersion = "1.3.0";
        PitMutantScoreSelector.descartesMode = false;
    }
}

