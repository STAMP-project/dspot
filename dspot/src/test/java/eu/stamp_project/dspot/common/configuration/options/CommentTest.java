package eu.stamp_project.dspot.common.configuration.options;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class CommentTest {

    private static final String TEST_CASE = "example.TestSuiteExample2";
    private static final String PATH_OUTPUT_TEST_CLASS = "target/dspot/output/example/TestSuiteExample2.java";

    @Before
    public void setUp() {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("src/test/resources/test-projects/target"));
        } catch (Exception ignored) {

        }
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(new File("target/trash"));
            FileUtils.deleteDirectory(new File("src/test/resources/test-projects/target"));
        } catch (Exception ignored) {

        }
    }

    private void assertFileStartsWith(String expectedStart) throws Exception{
        final File amplifiedTestClass = new File(CommentTest.PATH_OUTPUT_TEST_CLASS);

        try (BufferedReader reader = new BufferedReader(new FileReader(amplifiedTestClass))) {
            String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(content.startsWith(expectedStart));
        }
    }

    private void assertFileContains(String expected) throws Exception{
        final File amplifiedTestClass = new File(CommentTest.PATH_OUTPUT_TEST_CLASS);

        try (BufferedReader reader = new BufferedReader(new FileReader(amplifiedTestClass))) {
            String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(content.contains(expected));
        }
    }

    @Test
    public void testJacocoCoverageComments() throws Exception {
        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--amplifiers", "FastLiteralAmplifier",
                "--test-criterion", "JacocoCoverageSelector",
                "--test", TEST_CASE,
                "--clean",
                "--with-comment=All",
        });
        String expectedAmplifiedTestClassBegin = "package example;" + AmplificationHelper.LINE_SEPARATOR +
                "import org.junit.Assert;" + AmplificationHelper.LINE_SEPARATOR +
                "import org.junit.Test;" + AmplificationHelper.LINE_SEPARATOR +
                "public class TestSuiteExample2 {" + AmplificationHelper.LINE_SEPARATOR +
                "    private static int integer = TestResources.integer;" + AmplificationHelper.LINE_SEPARATOR +
                "" + AmplificationHelper.LINE_SEPARATOR +
                "    // JacocoCoverageSelector: Improves instruction coverage to 15/34" + AmplificationHelper.LINE_SEPARATOR +
                "    @Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "    public void test3_literalMutationString50_failAssert0() throws Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "        // AssertionGenerator generate try/catch block with fail statement" + AmplificationHelper.LINE_SEPARATOR +
                "        try {";

        assertTrue(new File(PATH_OUTPUT_TEST_CLASS).exists());
        assertFileStartsWith(expectedAmplifiedTestClassBegin);
    }

    @Test
    public void testDevFriendlyAllComments() throws Exception {
        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--amplifiers", "FastLiteralAmplifier",
                "--test-criterion", "ExtendedCoverageSelector",
                "--test", TEST_CASE,
                "--clean",
                "--with-comment=All",
                "--dev-friendly",
        });
        String classComment =
                "/* Coverage improved at" + AmplificationHelper.LINE_SEPARATOR +
                        "    example.Example:" + AmplificationHelper.LINE_SEPARATOR +
                        "    charAt" + AmplificationHelper.LINE_SEPARATOR  +
                        "    L. 7 +7 instr." + AmplificationHelper.LINE_SEPARATOR +
                        "     */";
        String amplifierComment =
                "        // AssertionGenerator: add assertion";
        assertTrue(new File(PATH_OUTPUT_TEST_CLASS).exists());
        assertFileContains(classComment);
        assertFileContains(amplifierComment);
    }

    @Test
    public void testAmplifierComments() throws Exception {
        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--amplifiers", "FastLiteralAmplifier",
                "--test-criterion", "ExtendedCoverageSelector",
                "--test", TEST_CASE,
                "--clean",
                "--with-comment=Amplifier",
                "--dev-friendly",
        });
        String commentAndExpression =
                "        // FastLiteralAmplifier: change number from '1' to '0.0'" + AmplificationHelper.LINE_SEPARATOR +
                "        // AssertionGenerator: create local variable with return value of invocation" + AmplificationHelper.LINE_SEPARATOR +
                "        char o_test3_literalMutationNumber41__4 = ex.charAt(s, s.length() - 0);";
        assertTrue(new File(PATH_OUTPUT_TEST_CLASS).exists());
        assertFileContains(commentAndExpression);
    }

    @Test
    public void testNoComments() throws Exception {
        Main.main(new String[]{
                "--verbose",
                "--absolute-path-to-project-root", new File("src/test/resources/test-projects/").getAbsolutePath() + "/",
                "--amplifiers", "FastLiteralAmplifier",
                "--test-criterion", "JacocoCoverageSelector",
                "--test", TEST_CASE,
                "--clean",
                "--with-comment=None",
                //"--dev-friendly",
        });
        String expectedAmplifiedTestClassBegin = "package example;" + AmplificationHelper.LINE_SEPARATOR +
                "import org.junit.Assert;" + AmplificationHelper.LINE_SEPARATOR +
                "import org.junit.Test;" + AmplificationHelper.LINE_SEPARATOR +
                "public class TestSuiteExample2 {" + AmplificationHelper.LINE_SEPARATOR +
                "    private static int integer = TestResources.integer;" + AmplificationHelper.LINE_SEPARATOR +
                "" + AmplificationHelper.LINE_SEPARATOR +
                "    @Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "    public void test3_literalMutationString50_failAssert0() throws Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "        try {" + AmplificationHelper.LINE_SEPARATOR +
                "            Example ex = new Example();" + AmplificationHelper.LINE_SEPARATOR +
                "            String s = \"\";" + AmplificationHelper.LINE_SEPARATOR +
                "            ex.charAt(s, s.length() - 1);" + AmplificationHelper.LINE_SEPARATOR +
                "            Assert.fail(\"test3_literalMutationString50 should have thrown StringIndexOutOfBoundsException\");" + AmplificationHelper.LINE_SEPARATOR +
                "        } catch (StringIndexOutOfBoundsException expected) {" + AmplificationHelper.LINE_SEPARATOR +
                "            Assert.assertEquals(\"String index out of range: 0\", expected.getMessage());" + AmplificationHelper.LINE_SEPARATOR +
                "        }" + AmplificationHelper.LINE_SEPARATOR +
                "    }";
        assertTrue(new File(PATH_OUTPUT_TEST_CLASS).exists());
        assertFileStartsWith(expectedAmplifiedTestClassBegin);
    }
}
