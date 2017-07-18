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
    }

    @Test
    public void runPit() throws Exception {
    }

    @Test
    public void runPit1() throws Exception {
    }

    private String[] getArgsWithGradleBuilder() throws IOException {
        return new String[]{
                "--path-to-properties", "src/test/resources/test-gradle-projects/test-projects.properties",
                "--test-criterion", "BranchCoverageTestSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdderOnAssert",
                "--iteration", "1",
                "--randomSeed", "72",
//                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-gradle-projects/test-projects.properties")),
                "--automatic-builder", "GradleBuilder",
                "--test", "all"
        };
    }

}