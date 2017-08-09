package example;

public class AmplTestSuiteExample {
	/* amplification of example.TestSuiteExample#test9 */
	@org.junit.Test(timeout = 10000)
	public void test9_cf2680() {
		example.Example notUsedExample = new example.Example();
		example.Example ex = new example.Example();
		// StatementAdderOnAssert create random local variable
		int vc_932 = -208865267;
		// AssertGenerator add assertion
		org.junit.Assert.assertEquals(-208865267, ((int) (vc_932)));
		// StatementAdderOnAssert create literal from method
		java.lang.String String_vc_10 = "abcdefghijklm";
		// AssertGenerator add assertion
		org.junit.Assert.assertEquals("abcdefghijklm", String_vc_10);
		// StatementAdderOnAssert create random local variable
		example.Example vc_929 = (example.Example)null;
		// AssertGenerator add assertion
		org.junit.Assert.assertNull(vc_929);
		// AssertGenerator create local variable with return value of invocation
		char o_test9_cf2680__10 = // StatementAdderMethod cloned existing statement
				ex.charAt(String_vc_10, vc_932);
		// AssertGenerator add assertion
		org.junit.Assert.assertEquals('a', ((char) (o_test9_cf2680__10)));
		org.junit.Assert.assertEquals('f', ex.charAt("abcdefghijklm", 5));
	}

}

