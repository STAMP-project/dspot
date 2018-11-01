package eu.stamp_project.automaticbuilder.maven;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/11/18
 */
public class DSpotPOMCreatorTest {

    @Test
    public void test() throws Exception {

        /*
            Test the copy and injection of the profile to run different foals in the origial pom.xml
         */

        InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        DSpotPOMCreator.createNewPom();

        try (BufferedReader reader = new BufferedReader(new FileReader(InputConfiguration.get().getAbsolutePathToProjectRoot() + DSpotPOMCreator.DSPOT_POM_FILE))) {
            final String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(content.endsWith(ENDS_WITH));
        }
    }

    private static final String ENDS_WITH = "";
}
