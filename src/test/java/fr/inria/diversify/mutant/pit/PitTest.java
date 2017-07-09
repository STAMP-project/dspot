package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.Utils;
import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.MavenAutomaticBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.*;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import javax.rmi.CORBA.Util;
import java.util.*;

import static org.junit.Assert.*;

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

    @Test
    public void testPitEvosuiteMode() throws Exception {

        /* by evosuite mode, we mean the common subset of mutation operators between pitest and evosuite */

        AmplificationHelper.setSeedRandom(23L);
        MavenPitCommandAndOptions.descartesMode = false;
        MavenPitCommandAndOptions.evosuiteMode = true;

        Utils.init(this.getPathToPropertiesFile());
        CtClass<Object> testClass = Utils.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        AutomaticBuilder builder = new MavenAutomaticBuilder(Utils.getInputConfiguration());

        List<PitResult> pitResults = builder.runPit(Utils.getInputProgram().getProgramDir(), testClass);

        System.out.println(pitResults);
        assertTrue(null != pitResults);

        assertEquals(8, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        Optional<PitResult> OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).findFirst();
        assertTrue(OptResult.isPresent());
        PitResult result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals(null, result.getMethod(testClass));
        assertEquals(27, result.getLineNumber());
        assertEquals("<init>", result.getLocation());

        assertEquals(8, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
        OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).findFirst();
        assertTrue(OptResult.isPresent());
        result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals("test4", result.getMethod(testClass).getSimpleName());
        assertEquals(18, result.getLineNumber());
        assertEquals("charAt", result.getLocation());

        assertEquals(2, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count());

        //todo should verify generated mutant
    }

    @Test
    public void testPit() throws Exception, InvalidSdkException {

        /*
            Run the PitRunner on the test-project example.
                Checks that the PitRunner return well the results, and verify state of mutant.
         */
        AmplificationHelper.setSeedRandom(23L);
        MavenPitCommandAndOptions.descartesMode = false;
        MavenPitCommandAndOptions.evosuiteMode = false;
        Utils.init(this.getPathToPropertiesFile());
        CtClass<Object> testClass = Utils.getInputProgram().getFactory().Class().get("example.TestSuiteExample");
        AutomaticBuilder builder = new MavenAutomaticBuilder(Utils.getInputConfiguration());
        List<PitResult> pitResults = builder.runPit(Utils.getInputProgram().getProgramDir(), testClass);

        assertTrue(null != pitResults);
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        Optional<PitResult> OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).findFirst();
        assertTrue(OptResult.isPresent());
        PitResult result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals(null, result.getMethod(testClass));
        assertEquals(27, result.getLineNumber());
        assertEquals("<init>", result.getLocation());

        assertEquals(13, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
        OptResult = pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).findFirst();
        assertTrue(OptResult.isPresent());
        result = OptResult.get();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", result.getFullQualifiedNameMutantOperator());
        assertEquals("test4", result.getMethod(testClass).getSimpleName());
        assertEquals(18, result.getLineNumber());
        assertEquals("charAt", result.getLocation());

        assertEquals(3, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.NO_COVERAGE).count());
    }

    @Test
    public void testFailPit() throws Exception, InvalidSdkException {

        /*
            Run the PitRunner in wrong configuration.
         */
        AutomaticBuilder builder = new MavenAutomaticBuilder(Utils.getInputConfiguration());
        List<PitResult> pitResults = builder.runPit(null, null);
        assertNull(pitResults);
    }
}
