package eu.stamp_project.automaticbuilder;

import eu.stamp_project.Utils;
import eu.stamp_project.mutant.pit.PitResult;
import eu.stamp_project.mutant.pit.PitResultParser;
import eu.stamp_project.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 08/09/17.
 */
public class GradleAutomaticBuilderWithDescartesTest {

    private AutomaticBuilder sut = null;

    @Before
    public void setUp() throws Exception {

        cleanTestEnv();

        Utils.init("src/test/resources/test-projects/test-projects.properties");

        Utils.LOGGER.debug("Test Set-up - Reading input parameters...");
        InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        inputConfiguration.setBuilderName("GradleBuilder");
        inputConfiguration.setDescartesMutators("1");

        Utils.LOGGER.debug("Test Set-up - instantiating Automatic Builder (SUT)...");
        sut = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration.getBuilderName());
        Utils.LOGGER.debug("Test Set-up complete.");
    }

    @After
    public void tearDown() throws Exception {
        Utils.LOGGER.debug("Test Tear-down...");

        cleanTestEnv();

        Utils.LOGGER.debug("Test Tear-down complete.");
    }


    @Test
    public void runPit_whenAllDescartesMutatorsAreSpecified() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder runPit() test when a test class is specified...");

        CtClass<Object> testClass = Utils.getInputConfiguration().getFactory().Class().get("example.TestSuiteExample");

        sut.runPit("src/test/resources/test-projects/", testClass);
        List<PitResult> pitResults = PitResultParser.parseAndDelete("src/test/resources/test-projects/" + sut.getOutputDirectoryPit());

        assertTrue("PIT results shouldn't be null", pitResults != null);
        assertTrue("PIT results shouldn't be empty", !pitResults.isEmpty());

        Utils.LOGGER.info("Gradle Automatic Builder runPit() test complete when a test class is specified.");
    }

    private void cleanTestEnv() throws IOException {
        File classPathFile = new File("src/test/resources/test-projects/gjp_cp");
        if (classPathFile.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting java classpath file...");
            classPathFile.delete();
        }

        File buildDir = new File("src/test/resources/test-projects/build");
        if (buildDir.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project build dir...");
            FileUtils.deleteDirectory(buildDir);
        }

        File gradlewDir = new File("src/test/resources/test-projects/gradle");
        if (gradlewDir.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project gradle dir...");
            FileUtils.deleteDirectory(gradlewDir);
        }

        File gradleHiddenDir = new File("src/test/resources/test-projects/.gradle");
        if (gradleHiddenDir.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project gradle hidden dir...");
            FileUtils.deleteDirectory(gradleHiddenDir);
        }

        Utils.LOGGER.debug("Test Env cleaning complete.");
    }

}
