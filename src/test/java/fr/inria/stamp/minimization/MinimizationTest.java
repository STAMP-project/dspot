package fr.inria.stamp.minimization;

import fr.inria.diversify.Utils;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.runner.InputConfiguration;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/17
 */
public class MinimizationTest {

	@Test
	public void testMinimize() throws Exception {

		InputConfiguration configuration =
				new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
		final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(configuration)
				.buildClasspath("src/test/resources/test-projects/");
		Launcher launcher = new Launcher();
		launcher.addInputResource("src/test/resources/test-projects/src/main/java/");
		launcher.addInputResource("src/test/resources/test-projects/src/test/java/");
		launcher.addInputResource("src/test/resources/AmplTestSuiteExample.java");//add extra amplified class

		launcher.getEnvironment().setCommentEnabled(true);

		launcher.getModelBuilder().setSourceClasspath(classpath.split(System.getProperty("path.separator")));
		launcher.buildModel();

		final CtMethod<?> test9_cf2680 = launcher.getFactory().Class()
				.get("example.AmplTestSuiteExample")
				.getMethodsByName("test9_cf2680").get(0);
		assertEquals(methodWithoutMinimization, test9_cf2680.toString());
		final CtMethod<?> minimize = Minimization.minimize(test9_cf2680);
		assertEquals(methodWithMinimization, minimize.toString());
	}

	private static final String nl = System.getProperty("line.separator");

	private static final String methodWithoutMinimization = "/* amplification of example.TestSuiteExample#test9 */" + nl  +
			"@org.junit.Test(timeout = 10000)" + nl  +
			"public void test9_cf2680() {" + nl  +
			"    example.Example notUsedExample = new example.Example();" + nl  +
			"    example.Example ex = new example.Example();" + nl  +
			"    // StatementAdderOnAssert create random local variable" + nl  +
			"    int vc_932 = -208865267;" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals((-208865267), ((int) (vc_932)));" + nl  +
			"    // StatementAdderOnAssert create literal from method" + nl  +
			"    java.lang.String String_vc_10 = \"abcdefghijklm\";" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals(\"abcdefghijklm\", String_vc_10);" + nl  +
			"    // StatementAdderOnAssert create random local variable" + nl  +
			"    example.Example vc_929 = ((example.Example) (null));" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertNull(vc_929);" + nl  +
			"    // AssertGenerator create local variable with return value of invocation" + nl  +
			"    // StatementAdderMethod cloned existing statement" + nl  +
			"    char o_test9_cf2680__10 = ex.charAt(String_vc_10, vc_932);" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals('a', ((char) (o_test9_cf2680__10)));" + nl  +
			"    org.junit.Assert.assertEquals('f', ex.charAt(\"abcdefghijklm\", 5));" + nl  +
			"}";

	private static final String methodWithMinimization = "/* amplification of example.TestSuiteExample#test9 */" + nl  +
			"@org.junit.Test(timeout = 10000)" + nl  +
			"public void test9_cf2680() {" + nl  +
			"    example.Example ex = new example.Example();" + nl  +
			"    // AssertGenerator add assertion" + nl  +
			"    org.junit.Assert.assertEquals('a', ex.charAt(\"abcdefghijklm\", (-208865267)));" + nl  +
			"    org.junit.Assert.assertEquals('f', ex.charAt(\"abcdefghijklm\", 5));" + nl  +
			"}";
}

