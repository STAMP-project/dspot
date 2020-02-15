package eu.stamp_project.dspot.common.automaticbuilder.maven;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import eu.stamp_project.dspot.selector.pitmutantscoreselector.AbstractPitResult;
import eu.stamp_project.dspot.selector.pitmutantscoreselector.PitXMLResultParser;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.Launcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilderTest {

    private PitXMLResultParser parser;

    private Launcher launcher;

    private UserInput configuration;

    private AutomaticBuilder builder;

    public void setUp(String path, String filter, boolean isDescartesMode) {
        this.setUp(path, filter, isDescartesMode, "");
    }

    public void setUp(String path, String filter, boolean isDescartesMode, String additionalClasspathElements) {
        this.parser = new PitXMLResultParser();
        this.configuration = new UserInput();
        this.configuration.setAbsolutePathToProjectRoot(new File(path).getAbsolutePath());
        this.configuration.setGregorMode(!isDescartesMode);
        this.configuration.setFilter(filter);
        this.configuration.setAdditionalClasspathElements(additionalClasspathElements);
        this.builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(this.configuration);
        DSpotPOMCreator.createNewPom(this.configuration);
        this.launcher = new Launcher();
        this.launcher.addInputResource(path + "/src/");
        this.launcher.getEnvironment().setNoClasspath(true);
        this.launcher.buildModel();
        DSpotState.verbose = true;
        try {
            FileUtils.deleteDirectory(new File(path + "/target/"));
        } catch (IOException ignored) {
            // ignored
        }
        builder.compileAndBuildClasspath();
    }

    @Test
    public void testGetDependenciesOf() throws Exception {
        UserInput configuration = new UserInput();
        configuration.setAbsolutePathToProjectRoot(new File("src/test/resources/test-projects/").getAbsolutePath());
        AutomaticBuilder builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        final String dependenciesOf = builder.buildClasspath();
        assertTrue(dependenciesOf.contains("org" + System.getProperty("file.separator") + "hamcrest" +
                System.getProperty("file.separator") + "hamcrest-core" + System.getProperty("file.separator") +
                "1.3" + System.getProperty("file.separator") + "hamcrest-core-1.3.jar"));
        assertTrue(dependenciesOf.contains("junit" + System.getProperty("file.separator") + "junit" +
                System.getProperty("file.separator") + "4.11" + System.getProperty("file.separator") +
                "junit-4.11.jar"));
    }

    @Test
    public void testRunPitDescartes() throws Exception {

        /*
            Test pit with descartes mutation engine.
                1- the number of mutants is considerably reduces
                2- the pom xml is modified before the reset()
                3- the reset() method restore the original pom.xml
         */

        setUp("src/test/resources/test-projects/", "", true);

        builder.runPit();
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(
                configuration.getAbsolutePathToProjectRoot() + builder.getOutputDirectoryPit()
        );

        assertEquals(2, pitResults.size());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(2, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
    }

    @Test
    public void testFailingPit() throws Exception {

        setUp("src/test/resources/mockito/", "", false);

        try {
            builder.runPit();
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }

        try {
            builder.runPit(launcher.getFactory().Class().get("info.sanaulla.dal.BookDALTest"));
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }
    }

    @Test
    public void testSpecificClass() throws Exception {

        setUp("src/test/resources/test-projects/", "", false);

        builder.runPit(launcher.getFactory().Class().get("example.TestSuiteExample2"));
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(
                configuration.getAbsolutePathToProjectRoot() + builder.getOutputDirectoryPit()
        );

        assertNotNull(pitResults);
        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
    }

    @Test
    public void testMultipleClasses() throws Exception {

        setUp("src/test/resources/test-projects/", "", false);

        builder.runPit(
                launcher.getFactory().Class().get("example.TestSuiteExample2"),
                launcher.getFactory().Class().get("example.TestSuiteExample")
        );
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(
                configuration.getAbsolutePathToProjectRoot() + builder.getOutputDirectoryPit()
        );

        assertNotNull(pitResults);
        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
    }

    @Test
    public void testOnProjectWithResources() throws Exception {
        setUp("src/test/resources/project-with-resources/", "", false, "src/test/resources/templates.jar");

        builder.runPit();
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(
                configuration.getAbsolutePathToProjectRoot() + builder.getOutputDirectoryPit()
        );

        assertNotNull(pitResults);
        assertEquals(91, pitResults.size());

    }

    @Test
    public void testUsingStarFilter() throws Exception {
        setUp("src/test/resources/test-projects/", "*", false);
        try {
            builder.runPit(
                    launcher.getFactory().Class().get("example.TestSuiteExample2"),
                    launcher.getFactory().Class().get("example.TestSuiteExample")
            );
            fail();
        } catch (Exception e) {

        }
    }
}
