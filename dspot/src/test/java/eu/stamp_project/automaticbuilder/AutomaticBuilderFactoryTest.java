package eu.stamp_project.automaticbuilder;

import eu.stamp_project.program.InputConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 14/07/17.
 */
public class AutomaticBuilderFactoryTest {

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAutomaticBuilder_whenMaven() throws Exception {

        InputConfiguration inputConfiguration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        inputConfiguration.setBuilderName("MAVEN");

        assertTrue(inputConfiguration.getBuilderName().toUpperCase().contains("MAVEN"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration.getBuilderName());

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenGradle() throws Exception {

        InputConfiguration inputConfiguration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        inputConfiguration.setBuilderName("GRADLE");

        assertTrue(inputConfiguration.getBuilderName().toUpperCase().contains("GRADLE"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration.getBuilderName());

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(GradleAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenUnknown() throws Exception {

        InputConfiguration inputConfiguration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        inputConfiguration.setBuilderName("UNKNOWNBuilder");

        assertFalse(inputConfiguration.getBuilderName() == null);
        assertFalse(inputConfiguration.getBuilderName().toUpperCase().contains("MAVEN"));
        assertFalse(inputConfiguration.getBuilderName().toUpperCase().contains("GRADLE"));

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration.getBuilderName());

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenConfDoesntContainBuilder() throws Exception {

        InputConfiguration inputConfiguration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");

        assertTrue(inputConfiguration.getBuilderName().isEmpty());

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration.getBuilderName());

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }
}