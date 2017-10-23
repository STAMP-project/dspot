package fr.inria.diversify.dspot;

import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.AmplificationHelper;

import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/05/17
 */
public class DSpotMultiplePomTest {

    @Test
    public void testCopyMultipleModuleProject() throws Exception {

        /*
            Contract: DSpot is able to amplify a multi-module project
         */

        final InputConfiguration configuration = new InputConfiguration("src/test/resources/multiple-pom/deep-pom-modules.properties");
        final DSpot dspot = new DSpot(configuration);
        int nbTestBeforeAmplification = AmplificationHelper.getAllTest(dspot.getInputProgram().getFactory().Class().get("HelloWorldTest")).size();
        final List<CtType> ctTypes = dspot.amplifyAllTests();
        assertFalse(ctTypes.isEmpty());
        assertTrue(ctTypes.get(0).getMethods().size() > nbTestBeforeAmplification);
    }
}
