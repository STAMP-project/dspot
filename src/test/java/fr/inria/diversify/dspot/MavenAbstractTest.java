package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.DSpotUtils;
import org.junit.After;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inriAVa.fr
 * on 1/5/17
 */
@Deprecated // TODO must be merge with AbstractTest
public abstract class MavenAbstractTest {

    public final String pathToPropertiesFile = getPathToPropertiesFile();

    public static final String nl = System.getProperty("line.separator");

    @Deprecated
    private static String originalProperties;

    @Before
    public void setUp() throws Exception {
        AutomaticBuilderFactory.reset();
    }

    @After
    public void tearDown() throws Exception {
        AutomaticBuilderFactory.reset();
    }

    public abstract String getPathToPropertiesFile();

    // hack to add maven.home to the properties automatically for travis. For local, the test will clean
    @Deprecated
    protected void addMavenHomeToPropertiesFile() {
        try (BufferedReader buffer = new BufferedReader(new FileReader(getPathToPropertiesFile()))) {
            originalProperties = buffer.lines().collect(Collectors.joining(nl));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String mavenHome = DSpotUtils.buildMavenHome(Utils.getInputConfiguration());
        if (mavenHome != null) {
            try(FileWriter writer = new FileWriter(getPathToPropertiesFile(), true)) {
                writer.write(nl + "maven.home=" + mavenHome + nl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Deprecated
    protected void removeHomFromPropertiesFile() {
        try(FileWriter writer = new FileWriter(pathToPropertiesFile, false)) {
            writer.write(originalProperties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
