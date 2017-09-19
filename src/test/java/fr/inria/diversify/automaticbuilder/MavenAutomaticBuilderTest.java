package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.Utils;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.stamp.Main;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilderTest {

    @After
    public void tearDown() throws Exception {
        Utils.reset();
    }

    @Test
    public void testGetDependenciesOf() throws Exception {

        try {
            FileUtils.forceDelete(new File("src/test/resources/test-projects//target/dspot/classpath"));
        } catch (Exception ignored){
            //ignored
        }

        Utils.reset();
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        final String dependenciesOf = Utils.getBuilder().buildClasspath("src/test/resources/test-projects/");
        assertTrue(dependenciesOf.contains("org" + System.getProperty("file.separator") + "hamcrest" +
                System.getProperty("file.separator") + "hamcrest-core" + System.getProperty("file.separator") +
                "1.3" + System.getProperty("file.separator") + "hamcrest-core-1.3.jar"));
        assertTrue(dependenciesOf.contains("junit" + System.getProperty("file.separator") + "junit" +
                System.getProperty("file.separator") + "4.11" + System.getProperty("file.separator") +
                "junit-4.11.jar"));
    }

    @Test
    public void testRunPit() throws Exception {

        Utils.reset();
        Utils.init("src/test/resources/test-projects/test-projects.properties");

        final List<PitResult> pitResults = Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir());

        assertEquals(25, pitResults.size());
        assertEquals(9, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.SURVIVED).count());
        assertEquals(13, pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED).count());
    }

    @Test
    public void testFailingPit() throws Exception {

        Utils.reset();
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
    public void testOnAbstractClass() throws Exception {

        Utils.reset();
        Utils.init("src/test/resources/sample/sample.properties");

        Main.verbose = true;

        final List<PitResult> pitResults = Utils.getBuilder().runPit(Utils.getInputProgram().getProgramDir(),
                Utils.findClass("fr.inria.inheritance.Inherited"));

        assertEquals(9, pitResults.size());
        System.out.println(pitResults);

        Main.verbose = true;
    }
}
