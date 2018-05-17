package eu.stamp_project.mutant.descartes;

import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/04/17
 */
public class DescartesTest {

    @Test
    public void testInjectionOfDescartesIfNeeded() throws Exception {
        final String pathname = "target/dspot/trash/pom.xml";
        try {
            FileUtils.copyFile(new File("src/test/resources/test-projects/pom.xml"), new File(pathname));
        } catch (Exception ignored) {
            FileUtils.forceDelete(new File(pathname));
            FileUtils.copyFile(new File("src/test/resources/test-projects/pom.xml"), new File(pathname));
        }
        assertTrue(DescartesChecker.shouldInjectDescartes(pathname));
        PitMutantScoreSelector.pitVersion = "1.4.0";
        DescartesInjector.injectDescartesIntoPom(pathname);
        assertFalse(DescartesChecker.shouldInjectDescartes(pathname));
        try (BufferedReader buffer = new BufferedReader(new FileReader(pathname))) {
            final String pomAsStr = buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            System.out.println(pomAsStr);
            assertTrue(pomAsStr.contains(expectedAddedDependencies));
            assertTrue(pomAsStr.contains(expectedAddedPlugin));
        } catch (IOException e) {
            fail("should not throw the exception " + e.toString());
        }
        FileUtils.forceDelete(new File(pathname));
    }

    private static final String expectedAddedDependencies = "<dependency><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.4.0</version></dependency><dependency><groupId>eu.stamp-project</groupId><artifactId>descartes</artifactId><version>1.2</version></dependency></dependencies>";

    private static final String expectedAddedPlugin = "<plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.4.0</version><configuration><mutationEngine>descartes</mutationEngine><mutators><mutator>void</mutator><mutator>null</mutator><mutator>true</mutator><mutator>false</mutator><mutator>empty</mutator><mutator>0</mutator><mutator>1</mutator><mutator>(byte)0</mutator><mutator>(byte)1</mutator><mutator>(short)1</mutator><mutator>(short)2</mutator><mutator>0L</mutator><mutator>1L</mutator><mutator>0.0</mutator><mutator>1.0</mutator><mutator>0.0f</mutator><mutator>1.0f</mutator><mutator>' '</mutator><mutator>'A'</mutator><mutator>\"\"</mutator><mutator>\"A\"</mutator></mutators></configuration><dependencies><dependency><groupId>eu.stamp-project</groupId><artifactId>descartes</artifactId><version>1.2</version></dependency></dependencies></plugin>";

}
