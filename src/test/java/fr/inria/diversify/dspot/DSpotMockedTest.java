package fr.inria.diversify.dspot;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/04/17
 */
public class DSpotMockedTest extends MavenAbstractTest {

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
        DSpot dspot = new DSpot(configuration, 1);
        addMavenHomeToPropertiesFile();
        try {
            FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        } catch (Exception ignored) {

        }
        CtType<?> amplifiedTest = dspot.amplifyTest("info.sanaulla.dal.BookDALTest", Collections.singletonList("testAddBook"));

        assertEquals(8, amplifiedTest.getMethods().size());
        final CtMethod<?> testAddBook_cf11_failAssert4 = amplifiedTest.getMethodsByName("testAddBook_cf11_failAssert4").get(0);
        assertEquals(expectedBody, testAddBook_cf11_failAssert4.getBody().toString());
        removeHomFromPropertiesFile();
    }

    private static final String nl = System.getProperty("line.separator");

    private static final String expectedBody = "{" + nl  +
            "    // AssertGenerator generate try/catch block with fail statement" + nl  +
            "    try {" + nl  +
            "        java.lang.String isbn = info.sanaulla.dal.AmplBookDALTest.mockedBookDAL.addBook(info.sanaulla.dal.AmplBookDALTest.book1);" + nl  +
            "        // StatementAdderOnAssert create null value" + nl  +
            "        info.sanaulla.models.Book vc_8 = (info.sanaulla.models.Book)null;" + nl  +
            "        // StatementAdderOnAssert create random local variable" + nl  +
            "        info.sanaulla.dal.BookDAL vc_7 = new info.sanaulla.dal.BookDAL();" + nl  +
            "        // StatementAdderMethod cloned existing statement" + nl  +
            "        vc_7.addBook(vc_8);" + nl  +
            "        // MethodAssertGenerator build local variable" + nl  +
            "        Object o_10_0 = info.sanaulla.dal.AmplBookDALTest.book1.getIsbn();" + nl  +
            "        org.junit.Assert.fail(\"testAddBook_cf11 should have thrown NullPointerException\");" + nl  +
            "    } catch (java.lang.NullPointerException eee) {" + nl  +
            "    }" + nl  +
            "}";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/mockito/mockito.properties";
    }
}
