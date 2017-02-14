package fr.inria.diversify.dspot.resources;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.TestRunner;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/14/17
 */
public class DSpotAndResourcesTest {

    @Test
    public void test() throws Exception, InvalidSdkException {
        final DSpot dSpot = new DSpot(new InputConfiguration("src/test/resources/sample/sample.properties"));
        InputProgram program = dSpot.getInputProgram();
        final CtClass<?> classUsingResources = program.getFactory().Class().get("fr.inria.testresources.TestResources");
        final String classpath = program.getProgramDir() + program.getClassesDir() + "/:" +
                program.getProgramDir() + program.getTestClassesDir() + "/";
        final JunitResult result = TestRunner.runTests(classUsingResources,
                classUsingResources.getMethodsByName("testResources"),
                classpath,
                program);

        assertTrue(new File("src/test/resources/aResource").exists());
        assertTrue(new File("./src/test/resources/aResource").exists());
        assertTrue(new File("src/test/resources/aResourcesDirectory/anotherResource").exists());
        assertTrue(new File("./src/test/resources/aResourcesDirectory/anotherResource").exists());
        assertTrue(result.getFailures().isEmpty());
        assertEquals(1, result.getTestRuns().size());
        assertEquals("testResources", result.getTestRuns().get(0));

        dSpot.cleanResources();

        assertFalse(new File("src/test/resources/aResourcesDirectory/anotherResource").exists());
        assertFalse(new File("./src/test/resources/aResourcesDirectory/anotherResource").exists());
    }


}
