package eu.stamp_project.minimization;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class GeneralMinimizerTest extends AbstractTest {

    /*
            The GeneralMinimizer is not directed by any test-criterion.
            The general minimization is done using static analysis of the code.
     */

    @Test
    public void testMinimizeInlineVariable() throws Exception {

        /*
          - redundant of local variable (e.g. a literal that just used one time can be in-lined)
         */

        final GeneralMinimizer generalMinimizer = new GeneralMinimizer();
        final CtMethod<?> minimize = generalMinimizer.minimize(Utils.findMethod("fr.inria.amplified.AmplifiedTest", "amplifiedTest"));
        final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    org.junit.Assert.assertEquals(5, 5);" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedBody, minimize.getBody().toString());
    }

    @Test
    public void testMinimizeDoNoRemoveRedundantAssertionWhenUsedBetweenThem() throws Exception {

        /*
            - redundant assertion must not be removed if the assertion value is used between the two assertions
         */

        final GeneralMinimizer generalMinimizer = new GeneralMinimizer();
        final CtMethod<?> minimize = generalMinimizer.minimize(Utils.findMethod("fr.inria.amplified.AmplifiedTest", "amplifiedTest2"));
        final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    java.lang.Integer __DSPOT_1 = 5;" + AmplificationHelper.LINE_SEPARATOR +
                "    org.junit.Assert.assertEquals(5, __DSPOT_1.intValue());" + AmplificationHelper.LINE_SEPARATOR +
                "    java.lang.System.out.println(__DSPOT_1.intValue());" + AmplificationHelper.LINE_SEPARATOR +
                "    org.junit.Assert.assertEquals(5, __DSPOT_1.intValue());" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedBody, minimize.getBody().toString());
    }

    @Test
    public void testMinimizeRemoveRedundantAssertion() throws Exception {

        /*
            - redundant assertion must be removed if the assertion value is not used between the two assertions
         */

        final GeneralMinimizer generalMinimizer = new GeneralMinimizer();
        final CtMethod<?> minimize = generalMinimizer.minimize(Utils.findMethod("fr.inria.amplified.AmplifiedTest", "amplifiedTest3"));
        final String expectedBody = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    java.lang.Integer __DSPOT_1 = 5;" + AmplificationHelper.LINE_SEPARATOR +
                "    org.junit.Assert.assertEquals(5, __DSPOT_1.intValue());" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedBody, minimize.getBody().toString());
    }
}
