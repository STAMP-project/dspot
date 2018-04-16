package fr.inria.diversify.automaticbuilder;

import fr.inria.Utils;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.Main;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilderTest {

    @Before
    public void setUp() throws Exception {
        Main.verbose = true;
    }

    @After
    public void tearDown() throws Exception {
        Main.verbose = false;
    }

    @Test
    public void testGetDependenciesOf() throws Exception {

        try {
            FileUtils.forceDelete(new File("src/test/resources/test-projects//target/dspot/classpath"));
        } catch (Exception ignored) {
            //ignored
        }

        Utils.init("src/test/resources/test-projects/test-projects.properties");

        final String dependenciesOf = Utils.getBuilder().buildClasspath("src/test/resources/test-projects/");
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

        Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir());
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
    }

    @Test
    public void testFailingPit() throws Exception {

        Utils.init("src/test/resources/mockito/mockito.properties");

        try {
            Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir());
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }

        try {
            Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir(),
                    Utils.findClass("info.sanaulla.dal.BookDALTest"));
            fail("Should have thrown a RuntimeException");
        } catch (RuntimeException e) {
            //success
        }
    }

    @Test
    public void testSpecificClass() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir(), Utils.findClass("example.TestSuiteExample2"));
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(28, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        assertEquals(15, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
    }

    @Test
    public void testMultipleClasses() throws Exception {
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir(), Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + Utils.getBuilder().getOutputDirectoryPit());

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

        Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir(),
                Utils.findClass("fr.inria.inheritance.Inherited"));

        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + Utils.getBuilder().getOutputDirectoryPit());

        assertEquals(31, pitResults.size());
    }

    @Test
    public void testOnProjectWithResources() throws Exception {

        Utils.init("src/test/resources/project-with-resources/project-with-resources.properties");

        Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir());
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(Utils.getInputProgram().getProgramDir() + Utils.getBuilder().getOutputDirectoryPit());

        assertNotNull(pitResults);
        assertEquals(88, pitResults.size());

    }

    @Test
    public void testUsingStarFilter() throws Exception {
        Main.verbose = true;
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        final InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        inputConfiguration.getProperties().setProperty("filter", "*");
        try {
            Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir(), Utils.findClass("example.TestSuiteExample2"), Utils.findClass("example.TestSuiteExample"));
            fail();
        } catch (Exception e) {

        }
        Main.verbose = false;
    }
}
