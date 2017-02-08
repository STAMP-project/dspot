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

        assertEquals(18, amplifiedTest.getMethods().size());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test2_cf16").getBody().toString());
    }

    private final String expectedAmplifiedBody = "{" + nl + 
            "    example.Example ex = new example.Example();" + nl + 
            "    // StatementAdderOnAssert create random local variable" + nl + 
            "    int vc_4 = 1635508580;" + nl + 
            "    // AssertGenerator add assertion" + nl + 
            "    org.junit.Assert.assertEquals(vc_4, 1635508580);" + nl + 
            "    // StatementAdderOnAssert create literal from method" + nl + 
            "    java.lang.String String_vc_0 = \"abcd\";" + nl + 
            "    // AssertGenerator add assertion" + nl + 
            "    org.junit.Assert.assertEquals(String_vc_0, \"abcd\");" + nl + 
            "    // StatementAdderOnAssert create random local variable" + nl + 
            "    example.Example vc_1 = new example.Example();" + nl + 
            "    // AssertGenerator replace invocation" + nl + 
            "    char o_test2_cf16__9 = // StatementAdderMethod cloned existing statement" + nl + 
            "vc_1.charAt(String_vc_0, vc_4);" + nl + 
            "    // AssertGenerator add assertion" + nl + 
            "    org.junit.Assert.assertEquals(o_test2_cf16__9, 'd');" + nl + 
            "    org.junit.Assert.assertEquals('d', ex.charAt(\"abcd\", 3));" + nl + 
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }
}
