package fr.inria.diversify.dspot;

import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;

import eu.stamp.project.testrunner.EntryPoint;
import fr.inria.stamp.Main;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/05/17
 */
public class DSpotMultiplePomTest {

    @Before
    public void setUp() throws Exception {
        try {
            FileUtils.forceDelete(new File("target/trash/"));
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {
            //ignored
        }
    }

    @Test
    public void testCopyMultipleModuleProject() throws Exception {

        /*
            Contract: DSpot is able to amplify a multi-module project
         */

        Main.verbose = true;
        EntryPoint.verbose = true;

        final InputConfiguration configuration = new InputConfiguration("src/test/resources/multiple-pom/deep-pom-modules.properties");
        final DSpot dspot = new DSpot(configuration, new JacocoCoverageSelector());
        final List<CtType> ctTypes = dspot.amplifyAllTests();
        assertFalse(ctTypes.isEmpty());

        EntryPoint.verbose = false;
        Main.verbose = false;
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.forceDelete(new File("target/trash/"));
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {
            //ignored
        }
    }
}
