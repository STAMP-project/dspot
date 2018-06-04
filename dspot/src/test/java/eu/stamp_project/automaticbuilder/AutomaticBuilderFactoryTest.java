package eu.stamp_project.automaticbuilder;

import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.Configuration;
import eu.stamp_project.JSAPOptions;
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

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAutomaticBuilder_whenMaven() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithMavenBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        inputConfiguration.setBuilderName(configuration.automaticBuilderName);

        assertTrue(inputConfiguration.getBuilderName().toUpperCase().contains("MAVEN"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenGradle() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithGradleBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        inputConfiguration.setBuilderName(configuration.automaticBuilderName);

        assertTrue(inputConfiguration.getBuilderName().toUpperCase().contains("GRADLE"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(GradleAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenUnknown() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithUnknownBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        inputConfiguration.setBuilderName(configuration.automaticBuilderName);

        assertFalse(inputConfiguration.getBuilderName() == null);
        assertFalse(inputConfiguration.getBuilderName().toUpperCase().contains("MAVEN"));
        assertFalse(inputConfiguration.getBuilderName().toUpperCase().contains("GRADLE"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenConfDoesntContainBuilder() throws Exception {

        this.configuration = JSAPOptions.parse(getArgsWithNoBuilder());

        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);

        assertTrue(inputConfiguration.getBuilderName().isEmpty());

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