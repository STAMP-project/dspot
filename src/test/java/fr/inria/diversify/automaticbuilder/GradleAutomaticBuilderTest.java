package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.stamp.Configuration;
import fr.inria.stamp.JSAPOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilderTest {

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    private Configuration configuration;

    AutomaticBuilderFactory factory = new AutomaticBuilderFactory();

    AutomaticBuilder sut = null;

    @Before
    public void setUp() throws Exception {
        this.configuration = JSAPOptions.parse(getArgsWithGradleBuilder());
        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        inputConfiguration.getProperties().setProperty("automaticBuilderName", configuration.automaticBuilderName);

        sut = factory.getAutomaticBuilder(inputConfiguration);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void compile() throws Exception {
    }

    @Test
    public void buildClasspath() throws Exception {
        String classPath = sut.buildClasspath("src/test/resources/test-projects/");

        assertNotNull(classPath, "Classpath should be null");

        assertTrue("Classpath should contain gradle-pitest-plugin-1.1.11.jar library as copmile/runtime dependency", classPath.contains("gradle-pitest-plugin-1.1.11.jar")); // compile dependency

        assertTrue("Classpath should contain hamcrest-core-1.3.jar library as test dependency", classPath.contains("hamcrest-core-1.3.jar")); // test compile dependency
        assertTrue("Classpath should contain junit-4.12.jar library as test dependency", classPath.contains("junit-4.12.jar")); // test compile dependency
    }

    @Test
    public void runPit() throws Exception {
    }

    @Test
    public void runPit1() throws Exception {
    }

    private String[] getArgsWithGradleBuilder() throws IOException {
        return new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "BranchCoverageTestSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
                "--automatic-builder", "GradleBuilder",
                "--test", "all"
        };
    }

}