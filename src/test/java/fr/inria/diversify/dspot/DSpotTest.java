package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/13/16
 */
public class DSpotTest extends MavenAbstractTest {

    @Test
    public void test() throws Exception, InvalidSdkException {

        /*
            Test the whole dspot procedure.
                It results with 18 methods: 7 manual + 13 amplified.
                The test consist of assert that the manual test remains, and there is an amplified version
         */

        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration);

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");

        System.out.println(amplifiedTest.getMethod("test1_cf24").getBody().toString());

        assertEquals(28, amplifiedTest.getMethods().size());
        assertEquals(originalTestBody, amplifiedTest.getMethod("test1").getBody().toString());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test1_cf24").getBody().toString());

        DSpotUtils.printJavaFileWithComment(amplifiedTest, new File("dspot-report"));
    }

    private final String originalTestBody = "{" + nl +
            "    example.Example ex = new example.Example();" + nl +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl +
            "}";

    private final String expectedAmplifiedBody = "{" + nl +
            "    example.Example ex = new example.Example();" + nl +
            "    // StatementAdderOnAssert create random local variable" + nl +
            "    int vc_5 = 1635508580;" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(vc_5, 1635508580);" + nl +
            "    // StatementAdderOnAssert create literal from method" + nl +
            "    java.lang.String vc_0 = \"abcd\";" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(vc_0, \"abcd\");" + nl +
            "    // StatementAdderOnAssert create random local variable" + nl +
            "    example.Example vc_1 = new example.Example();" + nl +
            "    // AssertGenerator replace invocation" + nl +
            "    char o_test1_cf24__9 = // StatementAdderMethod cloned existing statement" + nl +
            "vc_1.charAt(vc_0, vc_5);" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertEquals(o_test1_cf24__9, 'd');" + nl +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl +
            "}";

}
