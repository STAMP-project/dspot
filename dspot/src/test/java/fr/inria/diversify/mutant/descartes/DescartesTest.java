package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
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
        DescartesInjector.injectDescartesIntoPom(pathname);
        assertFalse(DescartesChecker.shouldInjectDescartes(pathname));
        try (BufferedReader buffer = new BufferedReader(new FileReader(pathname))) {
            final String pomAsStr = buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertEquals(expectedPom, pomAsStr);
        } catch (IOException e) {
            fail("should not throw the exception " + e.toString());
        }
        FileUtils.forceDelete(new File(pathname));
    }

    private static final String expectedPom = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">" + AmplificationHelper.LINE_SEPARATOR +
            "\t<modelVersion>4.0.0</modelVersion>" + AmplificationHelper.LINE_SEPARATOR +
            "\t<groupId>example</groupId>" + AmplificationHelper.LINE_SEPARATOR +
            "\t<artifactId>example</artifactId>" + AmplificationHelper.LINE_SEPARATOR +
            "\t<version>0.0.1-SNAPSHOT</version>" + AmplificationHelper.LINE_SEPARATOR +
            "\t<name>test-projects</name>" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "\t<properties>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t<default.encoding>UTF-8</default.encoding>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t<maven.compiler.source>1.7</maven.compiler.source>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t<maven.compiler.target>1.7</maven.compiler.target>" + AmplificationHelper.LINE_SEPARATOR +
            "\t</properties>" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "\t<build>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t<plugins>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t<plugin>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t\t<groupId>org.apache.maven.plugins</groupId>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t\t<artifactId>maven-compiler-plugin</artifactId>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t\t<version>3.7.0</version>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t\t<configuration>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t\t\t<source>1.8</source>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t\t\t<target>1.8</target>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t\t</configuration>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t</plugin>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t<plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>"+ PitMutantScoreSelector.pitVersion  +"</version><configuration><mutationEngine>descartes</mutationEngine><mutators><mutator>void</mutator><mutator>null</mutator><mutator>true</mutator><mutator>false</mutator><mutator>empty</mutator><mutator>0</mutator><mutator>1</mutator><mutator>(byte)0</mutator><mutator>(byte)1</mutator><mutator>(short)1</mutator><mutator>(short)2</mutator><mutator>0L</mutator><mutator>1L</mutator><mutator>0.0</mutator><mutator>1.0</mutator><mutator>0.0f</mutator><mutator>1.0f</mutator><mutator>' '</mutator><mutator>'A'</mutator><mutator>\"\"</mutator><mutator>\"A\"</mutator></mutators></configuration><dependencies><dependency><groupId>fr.inria.stamp</groupId><artifactId>descartes</artifactId><version>"+ PitMutantScoreSelector.descartesVersion  +"</version></dependency></dependencies></plugin></plugins>" + AmplificationHelper.LINE_SEPARATOR +
            "\t</build>" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "\t<dependencies>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t<dependency>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t<groupId>junit</groupId>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t<artifactId>junit</artifactId>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t\t<version>4.11</version>" + AmplificationHelper.LINE_SEPARATOR +
            "\t\t</dependency>" + AmplificationHelper.LINE_SEPARATOR +
            "\t<dependency><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.3.0</version></dependency><dependency><groupId>fr.inria.stamp</groupId><artifactId>descartes</artifactId><version>"+ PitMutantScoreSelector.descartesVersion  +"</version></dependency></dependencies>" + AmplificationHelper.LINE_SEPARATOR +
            "<repositories><repository><id>stamp-maven-repository-mvn-repo</id><url>https://stamp-project.github.io/stamp-maven-repository</url><snapshots><enabled>true</enabled><updatePolicy>always</updatePolicy></snapshots></repository></repositories></project>";
}
