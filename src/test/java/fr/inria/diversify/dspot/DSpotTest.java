package fr.inria.diversify.dspot;

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
                It results with 18 methods: 7 manual + 13 amplified.
                The test consist of assert that the manual test remains, and there is an amplified version
         */

        AmplificationHelper.setSeedRandom(23L);
        InputConfiguration configuration = new InputConfiguration(pathToPropertiesFile);
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration);

        CtType amplifiedTest = dspot.amplifyTest("example.TestSuiteExample");
        assertEquals(28, amplifiedTest.getMethods().size());
        assertEquals(originalTestBody, amplifiedTest.getMethod("test1").getBody().toString());
        assertEquals(expectedAmplifiedBody, amplifiedTest.getMethod("test1_cf24").getBody().toString());
    }


    private final String originalTestBody = "{" + nl +
            "    example.Example ex = new example.Example();" + nl +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl +
            "}";

    private final String expectedAmplifiedBody = "{" + nl +
            "    example.Example ex = new example.Example();" + nl +
            "    int vc_5 = 1635508580;" + nl +
            "    junit.framework.Assert.assertEquals(vc_5, 1635508580);" + nl +
            "    java.lang.String vc_0 = \"abcd\";" + nl +
            "    junit.framework.Assert.assertEquals(vc_0, \"abcd\");" + nl +
            "    example.Example vc_1 = new example.Example();" + nl +
            "    char o_test1_cf24__6 = vc_1.charAt(vc_0, vc_5);" + nl +
            "    junit.framework.Assert.assertEquals(o_test1_cf24__6, 'd');" + nl +
            "    org.junit.Assert.assertEquals('a', ex.charAt(\"abcd\", 0));" + nl +
            "}";

}
