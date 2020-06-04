package eu.stamp_project;

import eu.stamp_project.prettifier.Main;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class MainTest {

    @Before
    public void setUp() throws Exception {
        try {
            FileUtils.deleteDirectory(new File("target/dspot/output/"));
            FileUtils.deleteDirectory(new File("src/test/resources/sample/target"));
        } catch (Exception ignored) {

        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.deleteDirectory(new File("target/dspot/output/"));
            FileUtils.deleteDirectory(new File("src/test/resources/sample/target"));
        } catch (Exception ignored) {

        }
    }

    @Test
    public void testNoPrettifiers() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testApplyGeneralMinimizer() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--apply-general-minimizer"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testApplyPitMinimizer() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--apply-pit-minimizer"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testRenameTestMethods() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--path-to-code2vec", "src/test/resources/code2vec/code2vec",
                "--path-to-code2vec-model", "../model",
                "--rename-test-methods"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testRenameLocalVariables() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--path-to-code2vec", "src/test/resources/code2vec/code2vec",
                "--path-to-code2vec-model", "../model",
                "--rename-local-variables"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }
}
