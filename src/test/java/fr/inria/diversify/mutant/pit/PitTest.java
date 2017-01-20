package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.*;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PitTest extends MavenAbstractTest {

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }

    private final String pathToPropertiesFile = "src/test/resources/test-projects/test-projects.properties";
    private InputConfiguration inputConfiguration;
    private InputProgram inputProgram;
    private DSpotCompiler compiler;

    @Test
    public void testPit() throws Exception, InvalidSdkException {

        //TODO Should use Utils too (duplicated code)

        /*
            Run the PitRunner on the test-project example.
                Checks that the PitRunner return well the results, and verify state of mutant.
         */
        AmplificationHelper.setSeedRandom(23L);
        init();
        List<PitResult> pitResults = PitRunner.run(inputProgram, inputConfiguration, inputProgram.getFactory().Class().get("example.TestSuiteExample"));

        assertTrue(null != pitResults);
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        Optional<PitResult> OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).findFirst();
        assertTrue(OptResult.isPresent());
        PitResult result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals(null, result.getTestCaseMethod());
        assertEquals(27, result.getLineNumber());
        assertEquals("<init>", result.getLocation());

        assertEquals(13, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
        OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).findFirst();
        assertTrue(OptResult.isPresent());
        result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals("test4", result.getTestCaseMethod().getSimpleName());
        assertEquals(18, result.getLineNumber());
        assertEquals("charAt", result.getLocation());

        assertEquals(3, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count());
    }

    private void init() {
        try {
            inputConfiguration = new InputConfiguration(pathToPropertiesFile);
            inputProgram = InitUtils.initInputProgram(inputConfiguration);
            InitUtils.initLogLevel(inputConfiguration);
            inputConfiguration.setInputProgram(inputProgram);
            String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp";
            File tmpDir = new File(inputConfiguration.getProperty("tmpDir"));
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            } else {
                FileUtils.cleanDirectory(tmpDir);
            }
            String mavenHome = inputConfiguration.getProperty("maven.home", DSpotUtils.buildMavenHome());
            String mavenLocalRepository = inputConfiguration.getProperty("maven.localRepository", null);
            DSpotUtils.compileOriginalProject(this.inputProgram, mavenHome, mavenLocalRepository);
            FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
            inputProgram.setProgramDir(outputDirectory);
            String dependencies = AmplificationHelper.getDependenciesOf(inputConfiguration, inputProgram, mavenHome);
            File output = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
            FileUtils.cleanDirectory(output);
            DSpotCompiler.compile(inputProgram.getAbsoluteSourceCodeDir(), dependencies, output);
            compiler = new DSpotCompiler(inputProgram, dependencies);
            inputProgram.setFactory(compiler.getLauncher().getFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFailPit() throws Exception, InvalidSdkException {

        /*
            Run the PitRunner in wrong configuration.
         */
        try {
            List<PitResult> pitResults = PitRunner.run(null, null, null);
            fail("PirRunner.run() should throw : java.lang.NullPointerException");
        } catch (Exception e) {
            //catch the null pointer exception
        }
    }
}
