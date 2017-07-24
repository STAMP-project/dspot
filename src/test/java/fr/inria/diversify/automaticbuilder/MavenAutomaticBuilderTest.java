package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.Utils;
import org.junit.Test;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilderTest {



    @Test
    public void testWrongMaven() throws Exception {
        MavenAutomaticBuilder mavenAutomaticBuilder = new MavenAutomaticBuilder(Utils.getInputConfiguration());
        try {
            mavenAutomaticBuilder.buildClasspath("target/");
            fail("should have thrown FileNotFoundException");
        } catch (Exception expected) {
            //ignored
        }
    }

    @Test
    public void testGetDependenciesOf() throws Exception {
        MavenAutomaticBuilder mavenAutomaticBuilder = new MavenAutomaticBuilder(Utils.getInputConfiguration());
        final String dependenciesOf = mavenAutomaticBuilder.buildClasspath("src/test/resources/test-projects/");
        assertTrue(dependenciesOf.contains("org" + System.getProperty("file.separator") + "hamcrest" +
                System.getProperty("file.separator") + "hamcrest-core" + System.getProperty("file.separator") +
                "1.3" + System.getProperty("file.separator") + "hamcrest-core-1.3.jar"));
        assertTrue(dependenciesOf.contains("junit" + System.getProperty("file.separator") + "junit" +
                System.getProperty("file.separator") + "4.11" + System.getProperty("file.separator") +
                "junit-4.11.jar"));
    }

}
