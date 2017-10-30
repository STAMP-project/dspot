package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.Main;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class PitDescartesTest {

    @Before
    public void setUp() throws Exception {
        AmplificationHelper.setSeedRandom(23L);
        FileUtils.deleteDirectory(new File("target/dspot/trash"));
        FileUtils.deleteDirectory(new File("src/test/resources/descartes/target"));
        Main.verbose = true;
        MavenPitCommandAndOptions.descartesMode = false;
    }

    @Test
    public void testPitDescartesMode() throws Exception {
        assertFalse(MavenPitCommandAndOptions.descartesMode);
        MavenPitCommandAndOptions.descartesMode = true;
        InputConfiguration configuration = new InputConfiguration("src/test/resources/descartes/descartes.properties");
        DSpot dspot = new DSpot(configuration, 1,
                new PitMutantScoreSelector("src/test/resources/descartes/mutations.csv"));
        final CtClass<Object> originalTestClass = dspot.getInputProgram().getFactory().Class().get("fr.inria.stamp.mutationtest.test.TestCalculator");
        assertEquals(2, originalTestClass.getMethods().size());
        final CtType ctType = dspot.amplifyTest(
                "fr.inria.stamp.mutationtest.test.TestCalculator",
                Collections.singletonList("Integraltypestest")
        );
//        assertTrue(originalTestClass.getMethods().size() < ctType.getMethods().size()); // TODO
        FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        assertTrue(MavenPitCommandAndOptions.descartesMode);
    }

    @After
    public void tearDown() throws Exception {
        AutomaticBuilderFactory.reset();
        Main.verbose = false;
        MavenPitCommandAndOptions.descartesMode = false;
    }
}

