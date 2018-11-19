package eu.stamp_project.automaticbuilder.maven;

import eu.stamp_project.Utils;
import eu.stamp_project.mutant.pit.AbstractPitResult;
import eu.stamp_project.mutant.pit.PitCSVResult;
import eu.stamp_project.mutant.pit.PitCSVResultParser;
import eu.stamp_project.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilderTest {

    PitCSVResultParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new PitCSVResultParser();
    }

    @After
    public void tearDown() throws Exception {
        InputConfiguration.get().setDescartesMode(false);
    }

    @Test
    public void testGetDependenciesOf() throws Exception {

        try {
            FileUtils.forceDelete(new File("src/test/resources/test-projects//target/dspot/classpath"));
        } catch (Exception ignored) {
            //ignored
        }

        Utils.init("src/test/resources/test-projects/test-projects.properties");

        final String dependenciesOf = Utils.getBuilder().buildClasspath();
        assertTrue(dependenciesOf.contains("org" + System.getProperty("file.separator") + "hamcrest" +
                System.getProperty("file.separator") + "hamcrest-core" + System.getProperty("file.separator") +
                "1.3" + System.getProperty("file.separator") + "hamcrest-core-1.3.jar"));
        assertTrue(dependenciesOf.contains("junit" + System.getProperty("file.separator") + "junit" +
                System.getProperty("file.separator") + "4.11" + System.getProperty("file.separator") +
                "junit-4.11.jar"));
    }

    @Ignore // Overlapping with testOnProjectWithResources
    @Test
    public void testRunPit() throws Exception {

        Utils.init("src/test/resources/test-projects/test-projects.properties");
        InputConfiguration.get().setFilter("");

        DSpotPOMCreator.createNewPom();

        Utils.getBuilder().runPit();
        final List<AbstractPitResult> pitCSVResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(28, pitCSVResults.size());
        assertEquals(9, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.SURVIVED).count());
        assertEquals(15, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.KILLED).count());
    }

    @Test
    public void testRunPitDescartes() throws Exception {

        /*
            Test pit with descartes mutation engine.
                1- the number of mutants is considerably reduces
                2- the pom xml is modified before the reset()
                3- the reset() method restore the original pom.xml
         */

        Utils.init("src/test/resources/test-projects/test-projects.properties");
        InputConfiguration.get().setDescartesMode(true);
        InputConfiguration.get().setFilter("");

        DSpotPOMCreator.createNewPom();

        InputConfiguration.get().getBuilder().runPit();
        final List<AbstractPitResult> pitCSVResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(2, pitCSVResults.size());
        assertEquals(0, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.SURVIVED).count());
        assertEquals(2, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.KILLED).count());
    }

    @Test
    public void testFailingPit() throws Exception {

        Utils.init("src/test/resources/mockito/mockito.properties");
        InputConfiguration.get().setDescartesMode(false);
        DSpotPOMCreator.createNewPom();

        try {
            Utils.getBuilder().runPit();
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }

        try {
            Utils.getBuilder().runPit(Utils.findClass("info.sanaulla.dal.BookDALTest"));
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }
    }

    @Test
    public void testSpecificClass() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");
        InputConfiguration.get().setDescartesMode(false);
        DSpotPOMCreator.createNewPom();

        Utils.getBuilder().runPit(Utils.findClass("example.TestSuiteExample2"));
        final List<AbstractPitResult> pitCSVResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitCSVResults);
        assertEquals(28, pitCSVResults.size());
        assertEquals(9, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.SURVIVED).count());
        assertEquals(15, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.KILLED).count());
    }

    @Test
    public void testMultipleClasses() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");
        InputConfiguration.get().setDescartesMode(false);
        DSpotPOMCreator.createNewPom();

        Utils.getBuilder().runPit(Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
        final List<AbstractPitResult> pitCSVResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitCSVResults);
        assertEquals(28, pitCSVResults.size());
        assertEquals(9, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.SURVIVED).count());
        assertEquals(15, pitCSVResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitCSVResult.State.KILLED).count());
    }

    @Ignore
    @Test
    public void testOnAbstractClass() throws Exception {

        Utils.init("src/test/resources/sample/sample.properties");

        FileUtils.deleteDirectory(new File("src/test/resources/sample/target/pit-reports"));

        Utils.getBuilder().runPit(Utils.findClass("fr.inria.inheritance.Inherited"));

        final List<AbstractPitResult> pitCSVResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(31, pitCSVResults.size());
    }

    @Test
    public void testOnProjectWithResources() throws Exception {

        Utils.init("src/test/resources/project-with-resources/project-with-resources.properties");
        InputConfiguration.get().setDescartesMode(false);
        DSpotPOMCreator.createNewPom();

        Utils.getBuilder().runPit();
        final List<AbstractPitResult> pitCSVResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitCSVResults);
        assertEquals(88, pitCSVResults.size());

    }

    @Test
    public void testUsingStarFilter() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");
        InputConfiguration.get().setDescartesMode(false);
        final InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        inputConfiguration.setFilter("*");

        DSpotPOMCreator.createNewPom();
        try {
            Utils.getBuilder().runPit(Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
            fail();
        } catch (Exception e) {

        }
    }
}
