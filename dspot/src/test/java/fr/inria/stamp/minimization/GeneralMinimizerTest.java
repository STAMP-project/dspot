package fr.inria.stamp.minimization;

import fr.inria.Utils;
import fr.inria.AbstractTest;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class GeneralMinimizerTest extends AbstractTest {

    @Test
    public void testMinimize() throws Exception {

        /*
            The GeneralMinimizer is not directed by any test-criterion.
            The general minimization is done using static analysis of the code.
            Find a list of minimization done:
                - redundant of local variable (e.g. a literal that just used one time can be in-lined)
         */

        final GeneralMinimizer generalMinimizer = new GeneralMinimizer();
        final CtMethod<?> minimize = generalMinimizer.minimize(Utils.findMethod("fr.inria.amplified.AmplifiedTest", "amplifiedTest"));
        final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    org.junit.Assert.assertEquals(5, 5);" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedBody, minimize.getBody().toString());
    }
}
