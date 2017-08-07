package fr.inria.diversify.dspot;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;

import static org.junit.Assert.assertEquals;

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
		ValueCreator.count = 0;
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

		System.out.println(amplifiedTest);

		final CtMethod<?> testAddBook_cf11_failAssert4 = amplifiedTest.getMethodsByName("testAddBook_cf12").get(0);
		assertEquals(expectedBody, testAddBook_cf11_failAssert4.getBody().toString());
		removeHomFromPropertiesFile();
	}

	private static final String nl = System.getProperty("line.separator");

	private static final String expectedBody = "{" + nl  +
			"    java.lang.String isbn = info.sanaulla.dal.AmplBookDALTest.mockedBookDAL.addBook(info.sanaulla.dal.AmplBookDALTest.book1);" + nl  +
			"    org.junit.Assert.assertNotNull(isbn);" + nl  +
			"    // StatementAdderOnAssert create random local variable" + nl  +
			"    info.sanaulla.models.Book vc_9 = new info.sanaulla.models.Book(\"SgpbL[{$QV5:Wz2[|+mr\", \"6#-VtX(r!Fs2l>UgIvC=\", java.util.Collections.singletonList(\"U&zgYc TM1`_8;0L`A=S\"), \"O/woO!OKS@Rl&{ha!&Bc\", 279793993, -818917466, \"[?i!rb0/|]6^FT)-ef&b\");" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(\"SgpbL[{$QV5:Wz2[|+mr\", ((info.sanaulla.models.Book)vc_9).getIsbn());" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(279793993, ((int) (((info.sanaulla.models.Book)vc_9).getYearOfPublication())));" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertTrue(((info.sanaulla.models.Book)vc_9).getAuthors().contains(\"U&zgYc TM1`_8;0L`A=S\"));" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(\"O/woO!OKS@Rl&{ha!&Bc\", ((info.sanaulla.models.Book)vc_9).getPublication());" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(\"[?i!rb0/|]6^FT)-ef&b\", ((info.sanaulla.models.Book)vc_9).getImage());" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(-818917466, ((int) (((info.sanaulla.models.Book)vc_9).getNumberOfPages())));" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(\"6#-VtX(r!Fs2l>UgIvC=\", ((info.sanaulla.models.Book)vc_9).getTitle());" + nl  +
			"    // StatementAdderOnAssert create random local variable" + nl  +
			"    info.sanaulla.dal.BookDAL vc_7 = new info.sanaulla.dal.BookDAL();" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertTrue(((info.sanaulla.dal.BookDAL)vc_7).getAllBooks().isEmpty());" + nl  +
			"    // AssertGenerator create local variable with return value of invocation" + nl  +
			"    java.lang.String o_testAddBook_cf12__11 = // StatementAdderMethod cloned existing statement" + nl  +
			"    vc_7.addBook(vc_9);" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(\"SgpbL[{$QV5:Wz2[|+mr\", o_testAddBook_cf12__11);" + nl  +
			"    org.junit.Assert.assertEquals(info.sanaulla.dal.AmplBookDALTest.book1.getIsbn(), isbn);" + nl  +
			"}";

	@Override
	public String getPathToPropertiesFile() {
		return "src/test/resources/mockito/mockito.properties";
	}
}
