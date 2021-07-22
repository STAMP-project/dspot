package eu.stamp_project.dspot.common.automaticbuilder.gradle;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import eu.stamp_project.dspot.selector.pitmutantscoreselector.AbstractPitResult;
import eu.stamp_project.dspot.selector.pitmutantscoreselector.PitXMLResultParser;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleAutomaticBuilderTest.class);

    private AutomaticBuilder sut = null;

    private PitXMLResultParser parser;

    private final String pathToProjectRoot = "src/test/resources/test-projects/";

    private Launcher launcher;

    @Before
    public void setUp() throws Exception {
        DSpotState.verbose = true;
        cleanTestEnv();
        LOGGER.debug("Test Set-up - Reading input parameters...");
        LOGGER.debug("Test Set-up - instantiating Automatic Builder (SUT)...");
        final UserInput configuration = new UserInput();
        configuration.setAbsolutePathToProjectRoot(this.pathToProjectRoot);
        configuration.setGregorMode(true);
        configuration.setFilter("example.*");
        launcher = new Launcher();
        launcher.addInputResource(pathToProjectRoot + "src/");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        sut = AutomaticBuilderEnum.Gradle.getAutomaticBuilder(configuration);
        sut.compile();
        LOGGER.debug("Test Set-up complete.");
        parser = new PitXMLResultParser();

    }

    @After
    public void tearDown() throws Exception {
        LOGGER.debug("Test Tear-down...");
        cleanTestEnv();
        LOGGER.debug("Test Tear-down complete.");
    }

    @Test
    public void runPit_whenAllDescartesMutatorsAreSpecified() throws Exception {
        LOGGER.info("Starting Gradle Automatic Builder runPit() test when a test class is specified...");
        CtClass<Object> testClass = launcher.getFactory().Class().get("example.TestSuiteExample");
        sut.runPit( testClass);
        List<? extends AbstractPitResult> pitResults = parser.parseAndDelete("src/test/resources/test-projects/" + sut.getOutputDirectoryPit());
        assertTrue("PIT results shouldn't be null", pitResults != null);
        assertTrue("PIT results shouldn't be empty", !pitResults.isEmpty());
        LOGGER.info("Gradle Automatic Builder runPit() test complete when a test class is specified.");
    }

    private void cleanTestEnv() throws IOException {
        File classPathFile = new File("src/test/resources/test-projects/gjp_cp");
        if (classPathFile.exists()) {
            LOGGER.debug("Cleaning Test Env - Deleting java classpath file...");
            classPathFile.delete();
        }

        File buildDir = new File("src/test/resources/test-projects/build");
        if (buildDir.exists()) {
            LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project build dir...");
            FileUtils.deleteDirectory(buildDir);
        }

        File gradlewDir = new File("src/test/resources/test-projects/gradle");
        if (gradlewDir.exists()) {
            LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project gradle dir...");
            FileUtils.deleteDirectory(gradlewDir);
        }

        File gradleHiddenDir = new File("src/test/resources/test-projects/.gradle");
        if (gradleHiddenDir.exists()) {
            LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project gradle hidden dir...");
            FileUtils.deleteDirectory(gradleHiddenDir);
        }

        LOGGER.debug("Test Env cleaning complete.");
    }

}
