package eu.stamp_project.automaticbuilder;

import eu.stamp_project.automaticbuilder.gradle.GradleAutomaticBuilder;
import eu.stamp_project.automaticbuilder.maven.MavenAutomaticBuilder;
import eu.stamp_project.utils.options.AutomaticBuilderEnum;
import eu.stamp_project.utils.options.InputConfiguration;
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

//        InputConfiguration inputConfiguration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        final InputConfiguration inputConfiguration = InputConfiguration.get();
        inputConfiguration.setBuilderEnum(AutomaticBuilderEnum.Maven);

        assertTrue(inputConfiguration.getBuilderEnum().name().toUpperCase().contains("MAVEN"));

        AutomaticBuilder builder = inputConfiguration.getBuilderEnum().toAutomaticBuilder();

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(MavenAutomaticBuilder.class));
    }

    @Test
    public void getAutomaticBuilder_whenGradle() throws Exception {

//        InputConfiguration inputConfiguration = InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        final InputConfiguration inputConfiguration = InputConfiguration.get();
        inputConfiguration.setBuilderEnum(AutomaticBuilderEnum.Gradle);

        assertTrue(inputConfiguration.getBuilderEnum().name().toUpperCase().contains("GRADLE"));

        AutomaticBuilder builder = inputConfiguration.getBuilderEnum().toAutomaticBuilder();

        assertNotNull(builder);
        assertTrue(builder.getClass().equals(GradleAutomaticBuilder.class));
    }

}