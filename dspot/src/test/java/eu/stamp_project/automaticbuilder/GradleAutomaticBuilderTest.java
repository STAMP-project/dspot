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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilderTest {

    private AutomaticBuilder sut = null;

    @Before
    public void setUp() throws Exception {

        cleanTestEnv();

        Utils.init("src/test/resources/test-projects/test-projects.properties");

        Utils.LOGGER.debug("Test Set-up - Reading input parameters...");
        InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        inputConfiguration.setBuilderName("GradleBuilder");

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
    public void compile_whenCleanCompileTestTasksAreAppliedToDSpotTestExampleProject() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder compile() test...");
        sut.compile();

        File buildDir = new File("src/test/resources/test-projects/build");
        assertTrue("After compilation, build dir should exist", buildDir.exists());

        Utils.LOGGER.info("Gradle Automatic Builder compile() test complete.");
    }

    @Test
    public void buildClasspath_whenAppliedToDSpotTestExampleProject() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder buildClasspath() test...");

        String[] expectedCompilationLibraries = {"log4j-api-2.8.2.jar", "log4j-core-2.8.2.jar"};
        String[] expectedTestCompilationLibraries = {"hamcrest-core-1.3.jar", "junit-4.12.jar"};

        String classPath = sut.buildClasspath();

        assertNotNull(classPath, "Classpath should be null");

        assertTrue("Classpath should contain " + expectedCompilationLibraries[0] + " library as compile/runtime dependency", classPath.contains(expectedCompilationLibraries[0])); // compile dependency
        assertTrue("Classpath should contain " + expectedCompilationLibraries[1] + " library as compile/runtime dependency", classPath.contains(expectedCompilationLibraries[1])); // compile dependency

        assertTrue("Classpath should contain " + expectedTestCompilationLibraries[0] + " library as test dependency", classPath.contains(expectedTestCompilationLibraries[0])); // test compile dependency
        assertTrue("Classpath should contain " + expectedTestCompilationLibraries[1] + " library as test dependency", classPath.contains(expectedTestCompilationLibraries[1])); // test compile dependency

        Utils.LOGGER.info("Gradle Automatic Builder buildClasspath() test complete.");
    }

    @Test
    public void runPit_whenNoTestClassIsSpecified() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder runPit() test when no test class is specified...");

        sut.runPit("src/test/resources/test-projects/");
        List<PitResult> pitResults = PitResultParser.parseAndDelete("src/test/resources/test-projects/" + sut.getOutputDirectoryPit());

        assertTrue("PIT results shouldn't be null", pitResults != null);
        assertTrue("PIT results shouldn't be empty", !pitResults.isEmpty());

        Utils.LOGGER.info("Gradle Automatic Builder runPit() test complete when no test class is specified.");
    }

    @Test
    public void runPit_whenTestClassIsSpecified() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder runPit() test when a test class is specified...");

        CtClass<Object> testClass = Utils.getInputConfiguration().getFactory().Class().get("example.TestSuiteExample");

        sut.runPit("src/test/resources/test-projects/", testClass);
        List<PitResult> pitResults = PitResultParser.parseAndDelete("src/test/resources/test-projects/"+ sut.getOutputDirectoryPit());

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