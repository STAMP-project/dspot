package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import org.junit.After;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class MavenAbstractTest {

    public static final String pathToPropertiesFile = "src/test/resources/test-projects/test-projects.properties";

    public static final String nl = System.getProperty("line.separator");

    private static String originalProperties;

    @Before
    public void setUp() throws Exception {
        addMavenHomeToPropertiesFile();
    }

    @After
    public void tearDown() throws Exception {
        removeHomFromPropertiesFile();
    }

    // hack to add maven.home to the properties automatically for travis. For local, the test will clean
    private void addMavenHomeToPropertiesFile() {
        try (BufferedReader buffer = new BufferedReader(new FileReader(pathToPropertiesFile))) {
            originalProperties = buffer.lines().collect(Collectors.joining(nl));
        } catch (IOException ignored) {
            //ignored
        }
        final String mavenHome = Utils.buildMavenHome();
        if (mavenHome != null) {
            try(FileWriter writer = new FileWriter(pathToPropertiesFile, true)) {
                writer.write(nl + "maven.home=" + mavenHome + nl);
            } catch (IOException ignored) {
                //ignored
            }
        }
    }

    private void removeHomFromPropertiesFile() {
        try(FileWriter writer = new FileWriter(pathToPropertiesFile, false)) {
            writer.write(originalProperties);
        } catch (IOException ignored) {
            //ignored
        }
    }

}
