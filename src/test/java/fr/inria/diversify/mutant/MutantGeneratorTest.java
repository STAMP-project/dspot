package fr.inria.diversify.mutant;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                The generation results with XX mutants that is not killed by the original test suite.
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

    private final String pathToPropertiesFile = "src/test/resources/test-projects/test-projects.properties";

    private final String nl = System.getProperty("line.separator");

    private static String originalProperties;

    @Before
    public void setUp() throws Exception {
        addMavenHomeToPropertiesFile();
    }

    @After
    public void tearDown() throws Exception {
        removeHomFromPropertiesFile();
    }

    // hack to add maven.home to the properties automatically for travis. For local, the test will clean
    private void addMavenHomeToPropertiesFile() {
        try (BufferedReader buffer = new BufferedReader(new FileReader(pathToPropertiesFile))) {
            originalProperties = buffer.lines().collect(Collectors.joining(nl));
            System.out.println(originalProperties);
        } catch (IOException ignored) {
            //ignored
        }
        final String mavenHome = Utils.buildMavenHome();
        Log.debug("Maven Home: {}", mavenHome);
        try (FileWriter writer = new FileWriter(pathToPropertiesFile, true)) {
            writer.write(nl + "maven.home=" + mavenHome + nl);
        } catch (IOException ignored) {
            throw new RuntimeException(ignored);
            //ignored
        }
        try (BufferedReader buffer = new BufferedReader(new FileReader(pathToPropertiesFile))) {
            System.out.println(buffer.lines().collect(Collectors.joining(nl)));
        } catch (IOException ignored) {
            //ignored
        }
    }

    private void removeHomFromPropertiesFile() {
        try (FileWriter writer = new FileWriter(pathToPropertiesFile, false)) {
            writer.write(originalProperties);
        } catch (IOException ignored) {
            //ignored
        }
    }
}
