package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.FileUtils;
import org.junit.After;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class PitDescartesTest {

    //TODO The generation is not deterministic
    @Test
    public void testPitDescartesMode() throws Exception, InvalidSdkException {
        assertFalse(MavenPitCommandAndOptions.descartesMode);
        FileUtils.deleteDirectory(new File("target/dspot/trash"));
        AmplificationHelper.setSeedRandom(23L);
        MavenPitCommandAndOptions.descartesMode = true;
        InputConfiguration configuration = new InputConfiguration("src/test/resources/descartes/descartes.properties");
        DSpot dspot = new DSpot(configuration, 1,
                Collections.singletonList(new StatementAdd()),
                new PitMutantScoreSelector("src/test/resources/descartes/mutations.csv"));
        final CtClass<Object> originalTestClass = dspot.getInputProgram().getFactory().Class().get("fr.inria.stamp.mutationtest.test.TestCalculator");
        assertEquals(2, originalTestClass.getMethods().size());

        final CtType ctType = dspot.amplifyTest("fr.inria.stamp.mutationtest.test.TestCalculator",
                Collections.singletonList("Integraltypestest"));
        assertTrue(originalTestClass.getMethods().size() < ctType.getMethods().size());
        FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        assertTrue(MavenPitCommandAndOptions.descartesMode);
        MavenPitCommandAndOptions.descartesMode = false;
    }

    @After
    public void tearDown() throws Exception {
        AutomaticBuilderFactory.reset();
    }
}

