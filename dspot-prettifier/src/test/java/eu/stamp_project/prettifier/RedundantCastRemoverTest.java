package eu.stamp_project.prettifier;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import static org.junit.Assert.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/02/19
 */
public class RedundantCastRemoverTest {

    @Test
    public void test() {

        /*
            This test that we can remove some redundant cast.

            The last statement should keeps its cast
         */

        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/java/eu/stamp_project/resources/AmplifiedTest.java");
        launcher.buildModel();

        final Factory factory = launcher.getFactory();
        final CtMethod<?> redundantCast = factory.Class().get("eu.stamp_project.resources.AmplifiedTest")
                .getMethodsByName("redundantCast").get(0);

        final RedundantCastRemover redundantCastRemover = new RedundantCastRemover();
        final CtMethod<?> amplifiedTestWithoutRedundantCast = redundantCastRemover.remove(redundantCast);
        assertEquals(expected,
                amplifiedTestWithoutRedundantCast.getBody().toString()
        );
    }

    private static final String expected = "{\n" +
            "    final eu.stamp_project.resources.AmplifiedTest amplifiedTest = new eu.stamp_project.resources.AmplifiedTest();\n" +
            "    final eu.stamp_project.resources.AmplifiedTest.MyObject myObject = new eu.stamp_project.resources.AmplifiedTest.MyObject();\n" +
            "    // should be removed\n" +
            "    org.junit.Assert.assertEquals(0, amplifiedTest.getInt());\n" +
            "    org.junit.Assert.assertEquals(0, amplifiedTest.getInt());\n" +
            "    org.junit.Assert.assertEquals(0, myObject.getInt());\n" +
            "    org.junit.Assert.assertEquals(0, myObject.getMyInternalObject().getInt());\n" +
            "    org.junit.Assert.assertEquals(0, myObject.getMyInternalObject().getMySecondIntegernalObject().getInt());\n" +
            "    org.junit.Assert.assertEquals(0, myObject.getMyInternalObject().getMySecondIntegernalObject().getInt());\n" +
            "    // should not be removed\n" +
            "    org.junit.Assert.assertEquals(0, ((eu.stamp_project.resources.AmplifiedTest.MySecondInternalObject) (myObject.getMyInternalObject().getMySecondIntegernalObject().getObject())).getSecondInt());\n" +
            "}";
}
