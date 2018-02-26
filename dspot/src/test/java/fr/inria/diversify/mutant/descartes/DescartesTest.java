package fr.inria.diversify.mutant.descartes;

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

    // TODO implement

    private static final String nl = System.getProperty("line.separator");

    @Test
    @Ignore
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
            final String pomAsStr = buffer.lines().collect(Collectors.joining(nl));
            assertEquals(expectedPom, pomAsStr);
        } catch (IOException e) {
            fail("should not throw the exception " + e.toString());
        }
        FileUtils.forceDelete(new File(pathname));
    }

    private static final String expectedPom = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">" + nl  +
            "  <modelVersion>4.0.0</modelVersion>" + nl  +
            "  <groupId>example</groupId>" + nl  +
            "  <artifactId>example</artifactId>" + nl  +
            "  <version>0.0.1-SNAPSHOT</version>" + nl  +
            "  <name>test-projects</name>" + nl  +
            "" + nl  +
            "  <properties>" + nl  +
            "    <default.encoding>UTF-8</default.encoding>" + nl  +
            "    <maven.compiler.source>1.7</maven.compiler.source>" + nl  +
            "    <maven.compiler.target>1.7</maven.compiler.target>" + nl  +
            "  </properties>" + nl  +
            "" + nl  +
            "  <dependencies>" + nl  +
            "  \t<dependency>" + nl  +
            "  \t\t<groupId>junit</groupId>" + nl  +
            "  \t\t<artifactId>junit</artifactId>" + nl  +
            "  \t\t<version>4.11</version>" + nl  +
            "  \t</dependency>" + nl  +
            "  <dependency><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.1.11</version></dependency><dependency><groupId>fr.inria.stamp</groupId><artifactId>descartes</artifactId><version>0.1-SNAPSHOT</version></dependency></dependencies>" + nl +
            "<build><plugins><plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.1.11</version><configuration><mutationEngine>descartes</mutationEngine><mutators><mutator>null</mutator><mutator>void</mutator><mutator>0</mutator><mutator>false</mutator></mutators></configuration><dependencies><dependency><groupId>fr.inria.stamp</groupId><artifactId>descartes</artifactId><version>0.1-SNAPSHOT</version></dependency></dependencies></plugin></plugins></build><repositories><repository><id>stamp-maven-repository-mvn-repo</id><url>https://stamp-project.github.io/stamp-maven-repository</url><snapshots><enabled>true</enabled><updatePolicy>always</updatePolicy></snapshots></repository></repositories></project>";
}
