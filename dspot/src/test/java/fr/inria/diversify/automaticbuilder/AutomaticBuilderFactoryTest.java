package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
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
 * on 14/07/17.
 */
public class AutomaticBuilderFactoryTest {

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        AutomaticBuilderFactory.reset();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAutomaticBuilder_whenMaven() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithMavenBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        InputProgram program = InputConfiguration.initInputProgram(inputConfiguration);
        inputConfiguration.setInputProgram(program);
        inputConfiguration.getProperties().setProperty("automaticBuilderName", configuration.automaticBuilderName);

        assertTrue(inputConfiguration.getProperty("automaticBuilderName").toUpperCase().contains("MAVEN"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenGradle() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithGradleBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        inputConfiguration.getProperties().setProperty("automaticBuilderName", configuration.automaticBuilderName);

        assertTrue(inputConfiguration.getProperty("automaticBuilderName").toUpperCase().contains("GRADLE"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(GradleAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenUnknown() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithUnknownBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        inputConfiguration.getProperties().setProperty("automaticBuilderName", configuration.automaticBuilderName);
        InputProgram program = InputConfiguration.initInputProgram(inputConfiguration);
        inputConfiguration.setInputProgram(program);

        assertFalse(inputConfiguration.getProperty("automaticBuilderName") == null);
        assertFalse(inputConfiguration.getProperty("automaticBuilderName").toUpperCase().contains("MAVEN"));
        assertFalse(inputConfiguration.getProperty("automaticBuilderName").toUpperCase().contains("GRADLE"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenConfDoesntContainBuilder() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithNoBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        InputProgram program = InputConfiguration.initInputProgram(inputConfiguration);
        inputConfiguration.setInputProgram(program);

        assertTrue(inputConfiguration.getProperty("automaticBuilderName") == null);

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    private String[] getArgsWithMavenBuilder() throws IOException {
        return new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdd",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--automatic-builder", "MavenBuilder",
                "--test", "all"
        };
    }

    private String[] getArgsWithGradleBuilder() throws IOException {
        return new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdd",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--automatic-builder", "GradleBuilder",
                "--test", "all"
        };
    }

    private String[] getArgsWithUnknownBuilder() throws IOException {
        return new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdd",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--automatic-builder", "UNKNOWNBuilder",
                "--test", "all"
        };
    }

    private String[] getArgsWithNoBuilder() throws IOException {
        return new String[]{
                "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--amplifiers", "MethodAdd" + PATH_SEPARATOR + "TestDataMutator" + PATH_SEPARATOR + "StatementAdd",
                "--iteration", "1",
                "--randomSeed", "72",
                "--maven-home", DSpotUtils.buildMavenHome(new InputConfiguration("src/test/resources/test-projects/test-projects.properties")),
                "--test", "all"
        };
    }
}