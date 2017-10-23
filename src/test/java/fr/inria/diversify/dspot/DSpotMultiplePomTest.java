package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/05/17
 */
public class DSpotMultiplePomTest {

    //TODO this test is useless since we do not copy test
    @Test
    @Ignore
    public void testCopyMultipleModuleProject() throws Exception {

        /*
            This test that dspot copy the whole project, in order to keep the hierarchy of pom
                It will check that the whole hierarchy has been copied
         */

        Utils.init("src/test/resources/multiple-pom/deep-pom-modules.properties");

        final DSpot dSpot = new DSpot(Utils.getInputConfiguration());

        final StringBuilder currentPom = new StringBuilder(Utils.getInputConfiguration().getProperty("tmpDir") + "/tmp");
        assertTrue(new File(currentPom.toString() + "/pom.xml").exists());
        currentPom.append("/module-1");
        assertTrue(new File(currentPom.toString() + "/pom.xml").exists());
        assertTrue(new File(currentPom.toString() + "/module-2-1" + "/pom.xml").exists());
        assertTrue(new File(currentPom.toString() + "/module-2-2" + "/pom.xml").exists());
    }
}
