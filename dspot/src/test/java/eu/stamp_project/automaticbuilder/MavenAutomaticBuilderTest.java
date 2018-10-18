package eu.stamp_project.automaticbuilder;

import eu.stamp_project.Utils;
import eu.stamp_project.mutant.pit.PitResult;
import eu.stamp_project.mutant.pit.PitResultParser;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilderTest {

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

        Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot());
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
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
        String pomAsStr = "";
        try (BufferedReader buffer = new BufferedReader(new FileReader(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + "pom.xml"))) {
            pomAsStr = buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
        } catch (IOException e) {
            fail("should not throw the exception " + e.toString());
        }

        InputConfiguration.get().getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot());
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        try (BufferedReader buffer = new BufferedReader(new FileReader(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + "pom.xml"))) {
            assertNotEquals(buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)), pomAsStr);
        } catch (IOException e) {
            fail("should not throw the exception " + e.toString());
        }

        InputConfiguration.get().getBuilder().reset();

        try (BufferedReader buffer = new BufferedReader(new FileReader(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + "pom.xml"))) {
            assertEquals(buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)), pomAsStr);
        } catch (IOException e) {
            fail("should not throw the exception " + e.toString());
        }

        assertEquals(2, pitResults.size());
        assertEquals(0, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        assertEquals(2, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
    }

    @Test
    public void testFailingPit() throws Exception {

        Utils.init("src/test/resources/mockito/mockito.properties");

        try {
            Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot());
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }

        try {
            Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot(),
                    Utils.findClass("info.sanaulla.dal.BookDALTest"));
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }
    }

    @Test
    public void testSpecificClass() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot(), Utils.findClass("example.TestSuiteExample2"));
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
    }

    @Test
    public void testMultipleClasses() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot(), Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
    }

    @Ignore
    @Test
    public void testOnAbstractClass() throws Exception {

        Utils.init("src/test/resources/sample/sample.properties");

        FileUtils.deleteDirectory(new File("src/test/resources/sample/target/pit-reports"));

        Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot(),
                Utils.findClass("fr.inria.inheritance.Inherited"));

        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(31, pitResults.size());
    }

    @Test
    public void testOnProjectWithResources() throws Exception {

        Utils.init("src/test/resources/project-with-resources/project-with-resources.properties");

        Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot());
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputConfiguration().getAbsolutePathToProjectRoot() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(88, pitResults.size());

    }

    @Test
    public void testUsingStarFilter() throws Exception {
        Utils.getInputConfiguration().setVerbose(true);
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        final InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        inputConfiguration.setFilter("*");
        try {
            Utils.getBuilder().runPit(Utils.getInputConfiguration().getAbsolutePathToProjectRoot(), Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
            fail();
        } catch (Exception e) {

        }
        Utils.getInputConfiguration().setVerbose(false);
    }
}
