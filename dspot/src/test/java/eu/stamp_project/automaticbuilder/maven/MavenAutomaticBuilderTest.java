package eu.stamp_project.automaticbuilder.maven;

import eu.stamp_project.Utils;
import eu.stamp_project.testrunner.listener.pit.AbstractPitResult;
import eu.stamp_project.testrunner.listener.pit.PitXMLResultParser;
import eu.stamp_project.utils.program.InputConfiguration;
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

    PitXMLResultParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new PitXMLResultParser();
    }

    @After
    public void tearDown() throws Exception {
        InputConfiguration.get().setDescartesMode(false);
    }

    @Test
    public void testRunPitJUnit5() {

        /*
            test that we can run pit using maven on junit 5 tests
         */

        Utils.init("src/test/resources/sample/sample.properties");
        InputConfiguration.get().setDescartesMode(true);
        InputConfiguration.get().setJUnit5(true);
        InputConfiguration.get().setPitFilterClassesToKeep("fr.inria.pit.*");

        DSpotPOMCreator.createNewPom();

        InputConfiguration.get().getBuilder().runPit(
                Utils.findClass("fr.inria.pit.junit5.TestClass")
        );
        final List<? extends AbstractPitResult> pitResults =
                parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(1, pitResults.size());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());

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
        InputConfiguration.get().setPitFilterClassesToKeep("");

        DSpotPOMCreator.createNewPom();

        Utils.getBuilder().runPit();
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
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
        InputConfiguration.get().setPitFilterClassesToKeep("");

        DSpotPOMCreator.createNewPom();

        InputConfiguration.get().getBuilder().runPit();
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(2, pitResults.size());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(2, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
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
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(95, pitResults.size());
        assertEquals(42, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(45, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
    }

    @Test
    public void testMultipleClasses() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");
        InputConfiguration.get().setDescartesMode(false);
        DSpotPOMCreator.createNewPom();

        Utils.getBuilder().runPit(Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(95, pitResults.size());
        assertEquals(42, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.SURVIVED).count());
        assertEquals(45, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
    }

    @Ignore
    @Test
    public void testOnAbstractClass() throws Exception {

        Utils.init("src/test/resources/sample/sample.properties");

        FileUtils.deleteDirectory(new File("src/test/resources/sample/target/pit-reports"));

        Utils.getBuilder().runPit(Utils.findClass("fr.inria.inheritance.Inherited"));

        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(31, pitResults.size());
    }

    @Test
    public void testOnProjectWithResources() throws Exception {

        Utils.init("src/test/resources/project-with-resources/project-with-resources.properties");
        InputConfiguration.get().setDescartesMode(false);
        DSpotPOMCreator.createNewPom();

        Utils.getBuilder().runPit();
        final List<? extends AbstractPitResult> pitResults = parser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(136, pitResults.size());

    }

    @Test
    public void testUsingStarFilter() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");
        InputConfiguration.get().setDescartesMode(false);
        final InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        inputConfiguration.setPitFilterClassesToKeep("*");

        DSpotPOMCreator.createNewPom();
        try {
            Utils.getBuilder().runPit(Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
            fail();
        } catch (Exception e) {

        }
    }
}
