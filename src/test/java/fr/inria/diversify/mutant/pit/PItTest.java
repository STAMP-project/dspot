package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.MavenAbstractTest;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

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

        InputConfiguration inputConfiguration = new InputConfiguration(pathToPropertiesFile);
        InitUtils.initLogLevel(inputConfiguration);
        InputProgram inputProgram = InitUtils.initInputProgram(inputConfiguration);
        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);
        InitUtils.initDependency(inputConfiguration);
        DSpotUtils.compileTests(inputProgram, inputConfiguration.getProperty("maven.home", null), inputConfiguration.getProperty("maven.localRepository", null));
        DSpotCompiler.buildCompiler(inputProgram, true);

        List<PitResult> pitResults = PitRunner.run(inputProgram, inputConfiguration, inputProgram.getFactory().Class().get("example.TestSuiteExample"));

        assertTrue(null != pitResults);
        assertEquals(3, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        Optional<PitResult> OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).findFirst();
        assertTrue(OptResult.isPresent());
        PitResult result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals("none", result.getFullQualifiedNameTestMethod());
        assertEquals(24, result.getLineNumber());
        assertEquals("<init>", result.getLocation());

        assertEquals(6, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
        OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).findFirst();
        assertTrue(OptResult.isPresent());
        result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals("example.TestSuiteExample.test4(example.TestSuiteExample)", result.getFullQualifiedNameTestMethod());
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
