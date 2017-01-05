package fr.inria.diversify.mutant;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static fr.inria.diversify.dspot.MavenAbstractTest.pathToPropertiesFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/3/17
 */
public class MutantGeneratorTest {

    @Test
    public void testMutantGenerator() throws Exception, InvalidSdkException {

        /*
            Test the generation of mutant on the test-projects example.
                The generation results with 12 mutants that is not killed by the original test suite.
            Then, we run an amplification and check that we effectively kill more mutant that the original test suite with it.
         */
        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        final File outputDirectory = new File(configuration.getProjectPath() + "/" + configuration.getRelativeTestSourceCodeDir());
        List<Amplifier> amplifiers = Collections.singletonList(new TestDataMutator());
        MutantGenerator mutantGenerator = new MutantGenerator(pathToPropertiesFile);
        DSpot dspot = new DSpot(configuration, 1, amplifiers);

        final CtClass<Object> exampleOriginalClass = dspot.getInputProgram().getFactory().Class().get("example.Example");
        final CtClass<Object> exampleOriginalTestClass = dspot.getInputProgram().getFactory().Class().get("example.TestSuiteExample");

        mutantGenerator.generateForAllClasses();
        Map<String, CtClass> mutantsNotKilled = mutantGenerator.getMutantsNotKilled();

        assertEquals(12, mutantsNotKilled.size());
        mutantsNotKilled.keySet().forEach(key -> {
            assertNotEquals(exampleOriginalClass, mutantsNotKilled.get(key));
        });

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");

        PrintClassUtils.printJavaFile(outputDirectory, amplifiedTest);

        MutantRunResults mutantResultsWithAmplifiedTests = mutantGenerator.runTestsOnAliveMutant(new InputConfiguration(pathToPropertiesFile));
        assertTrue(mutantsNotKilled.size() > mutantResultsWithAmplifiedTests.getRemainsAliveMutant().size());
        assertEquals(mutantsNotKilled.size(), mutantResultsWithAmplifiedTests.getRemainsAliveMutant().size() +
                mutantResultsWithAmplifiedTests.getKilledMutants().size());

        PrintClassUtils.printJavaFile(outputDirectory, exampleOriginalTestClass);
    }
}
