package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/13/16
 */
public class DSpotTest extends MavenAbstractTest {

    @Test
    public void test() throws Exception, InvalidSdkException {

        Utils.reset();

        /*
            Test the whole dspot procedure.
                It results with 24 methods: 18 amplified tests + 6 original tests.
         */

        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration);

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");

        assertEquals(24, amplifiedTest.getMethods().size());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test2_cf24").getBody().toString());

        assertEquals(28, amplifiedTest.getMethods().size());
        //assertEquals(originalTestBody, amplifiedTest.getMethod("test1").getBody().toString());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test1_cf10").getBody().toString());
    }

    private final String expectedAmplifiedBody = "{\n" +
            "    example.Example ex = new example.Example();\n" +
            "    // StatementAdderOnAssert create random local variable\n" +
            "    int vc_5 = 1635508580;\n" +
            "    // AssertGenerator add assertion\n" +
            "    org.junit.Assert.assertEquals(vc_5, 1635508580);\n" +
            "    // StatementAdderOnAssert create literal from method\n" +
            "    java.lang.String vc_0 = \"abcd\";\n" +
            "    // AssertGenerator add assertion\n" +
            "    org.junit.Assert.assertEquals(vc_0, \"abcd\");\n" +
            "    // StatementAdderOnAssert create random local variable\n" +
            "    example.Example vc_1 = new example.Example();\n" +
            "    // AssertGenerator replace invocation\n" +
            "    char o_test2_cf24__9 = // StatementAdderMethod cloned existing statement\n" +
            "vc_1.charAt(vc_0, vc_5);\n" +
            "    // AssertGenerator add assertion\n" +
            "    org.junit.Assert.assertEquals(o_test2_cf24__9, 'd');\n" +
            "    org.junit.Assert.assertEquals('d', ex.charAt(\"abcd\", 3));\n" +
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
