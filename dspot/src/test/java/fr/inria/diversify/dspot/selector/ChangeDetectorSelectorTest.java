package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.Main;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/08/17
 */
public class ChangeDetectorSelectorTest {

    // TODO this is not deterministic
    @Test
    public void test() throws Exception {

        final String configurationPath = "src/test/resources/regression/test-projects_0/test-projects.properties";
        final ChangeDetectorSelector changeDetectorSelector = new ChangeDetectorSelector();

        final InputConfiguration configuration = new InputConfiguration(configurationPath);
        final DSpot dSpot = new DSpot(configuration, 1,
                Collections.singletonList(new StatementAdd()),
                changeDetectorSelector);
        assertEquals(6, dSpot.getInputProgram().getFactory().Type().get("example.TestSuiteExample").getMethods().size());
        final CtType<?> ctType = dSpot.amplifyTest("example.TestSuiteExample").get(0); // TODO
        assertFalse(ctType.getMethods().isEmpty());// TODO this is not deterministic.
        // TODO We verify that DSpot has been able to detect the changes between the two version
        // TODO at least with one amplified test, i.e. the list of method returned amplified test is not empty
    }

    @Test
    public void testOnMultiModuleProject() throws Exception {

        Main.verbose = true;

		/*
            Test that we can use the Change Detector on a multi module project
				The amplification is still done on one single module.
				DSpot should be able to return an amplified test that catch changes.
		 */

        try {
            FileUtils.forceDelete(new File("src/test/resources/multiple-pom/module-1/module-2-1/target"));
        } catch (Exception ignored) {

        }
        try {
            FileUtils.forceDelete(new File("src/test/resources/multiple-pom_1/module-1/module-2-1/target"));
        } catch (Exception ignored) {

        }

        try {
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {

        }

        final String configurationPath = "src/test/resources/multiple-pom/deep-pom-modules.properties";
        final ChangeDetectorSelector changeDetectorSelector = new ChangeDetectorSelector();
        final InputConfiguration configuration = new InputConfiguration(configurationPath);
        final DSpot dSpot = new DSpot(configuration, 1,
                Collections.singletonList(new StatementAdd()),
                changeDetectorSelector);
        assertFalse(dSpot.amplifyAllTests().isEmpty());

        Main.verbose = false;
    }
}
