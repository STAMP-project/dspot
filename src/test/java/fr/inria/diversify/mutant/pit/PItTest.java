package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.*;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PItTest extends MavenAbstractTest {

    private final String pathToPropertiesFile = "src/test/resources/test-projects/test-projects.properties";

    @Test
    public void testPit() throws Exception, InvalidSdkException {

        /*
            Run the PitRunner on the test-project example.
                Checks that the PitRunner return well the results, and verify state of mutant.
         */
        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration inputConfiguration = new InputConfiguration(pathToPropertiesFile);
        InitUtils.initLogLevel(inputConfiguration);
        InputProgram inputProgram = InitUtils.initInputProgram(inputConfiguration);
        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);
        InitUtils.initDependency(inputConfiguration);
        String mavenHome = inputConfiguration.getProperty("maven.home", null);
        String mavenLocalRepository = inputConfiguration.getProperty("maven.localRepository", null);
        DSpotUtils.compile(inputProgram, mavenHome, mavenLocalRepository);
        DSpotUtils.initClassLoader(inputProgram, inputConfiguration);
        DSpotCompiler.buildCompiler(inputProgram, true);
        DSpotUtils.compileTests(inputProgram, mavenHome, mavenLocalRepository);
        InitUtils.initLogLevel(inputConfiguration);

        List<PitResult> pitResults = PitRunner.run(inputProgram, inputConfiguration, inputProgram.getFactory().Class().get("example.TestSuiteExample"));

        assertTrue(null != pitResults);
        assertEquals(3, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        Optional<PitResult> OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).findFirst();
        assertTrue(OptResult.isPresent());
        PitResult result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals(null, result.getTestCaseMethod());
        assertEquals(24, result.getLineNumber());
        assertEquals("<init>", result.getLocation());

        assertEquals(6, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
        OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).findFirst();
        assertTrue(OptResult.isPresent());
        result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals("test4", result.getTestCaseMethod().getSimpleName());
        assertEquals(18, result.getLineNumber());
        assertEquals("charAt", result.getLocation());

        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count());
    }

    @Test
    public void testFailPit() throws Exception, InvalidSdkException {

        /*
            Run the PitRunner in wrong configuration.
         */

        List<PitResult> pitResults = PitRunner.run(null, null, null);

        assertTrue(null == pitResults);
    }
}
