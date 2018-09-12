package eu.stamp_project.utils;

import eu.stamp_project.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/20/17
 */
public class DSpotUtilsTest {

    private final static File outputDirectory = new File("target/trash/");

    @Before
    public void setUp() throws Exception {
        InputConfiguration.get().setWithComment(true);
    }

    @After
    public void tearDown() throws Exception {
        InputConfiguration.get().setWithComment(false);
    }

    @Test
    public void testPrintAmplifiedTestClass() throws Exception {
        final File javaFile = new File(outputDirectory.getAbsolutePath() + "/" + "example.TestSuiteExample".replaceAll("\\.", "\\/") + ".java");
        try {
            FileUtils.forceDelete(javaFile);
        } catch (IOException ignored) {
            //ignored
        }

        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/resources/test-projects/src/test/java/example/TestSuiteExample.java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        final CtType<?> type = launcher.getFactory().Type().get("example.TestSuiteExample");

        assertFalse(javaFile.exists());
        DSpotUtils.printAmplifiedTestClass(type, outputDirectory);
        assertTrue(javaFile.exists());

        final CtMethod<?> clone = type.getMethods().stream()
                .findFirst().get().clone();
        final int nbMethodStart = type.getMethods().size();
        type.getMethods().forEach(type::removeMethod);
        clone.setSimpleName("MyNewMethod");
        type.addMethod(clone);

        DSpotUtils.printAmplifiedTestClass(type, outputDirectory);
        launcher = new Launcher();
        launcher.addInputResource(outputDirectory.getAbsolutePath() + "/" + "example.TestSuiteExample".replaceAll("\\.", "\\/") + ".java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();

        assertEquals(nbMethodStart + 1, launcher.getFactory().Class().get("example.TestSuiteExample").getMethods().size());

        type.getMethods().forEach(type::removeMethod);
        clone.setSimpleName("MyNewMethod2");
        type.addMethod(clone);

        DSpotUtils.printAmplifiedTestClass(type, outputDirectory);
        launcher = new Launcher();
        launcher.addInputResource(outputDirectory.getAbsolutePath() + "/" + "example.TestSuiteExample".replaceAll("\\.", "\\/") + ".java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();

        assertEquals(nbMethodStart + 2, launcher.getFactory().Class().get("example.TestSuiteExample").getMethods().size());
    }


}
