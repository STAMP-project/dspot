package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdderOnAssert;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.amplifier.TestMethodCallAdder;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.mutant.pit.PitRunner;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.stamp.JSAPOptions;
import fr.inria.stamp.Main;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class PitDescartesTest {

    private static final String nl = System.getProperty("line.separator");

    @Test
    public void testPitDescartesMode() throws Exception, InvalidSdkException {
        assertFalse(PitRunner.descartesMode);
        FileUtils.deleteDirectory(new File("dspot-out"));

        AmplificationHelper.setSeedRandom(23L);
        PitRunner.descartesMode = true;
        InputConfiguration configuration = new InputConfiguration("src/test/resources/descartes/descartes.properties");
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration, 3,
                Arrays.asList(new TestMethodCallAdder(), new TestDataMutator(), new StatementAdderOnAssert()),
                new PitMutantScoreSelector());

        final CtClass<Object> originalTestClass = dspot.getInputProgram().getFactory().Class().get("fr.inria.stamp.mutationtest.test.TestCalculator");
        assertEquals(2 ,originalTestClass.getMethods().size());

        final CtType ctType = dspot.amplifyTest("fr.inria.stamp.mutationtest.test.TestCalculator");
        assertEquals(8, ctType.getMethods().size());

        final CtMethod<?> integraltypestest_cf1237 = (CtMethod<?>) ctType.getMethodsByName("Integraltypestest_cf1237").get(0);
        assertEquals(expectedBody, integraltypestest_cf1237.getBody().toString());

        FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));

        assertTrue(PitRunner.descartesMode);
    }

    private static final String expectedBody = "{" + nl +
            "    final fr.inria.stamp.mutationtest.test.Calculator calculator = new fr.inria.stamp.mutationtest.test.Calculator();" + nl +
            "    org.junit.Assert.assertEquals(((byte) (0)), calculator.getByte());" + nl +
            "    org.junit.Assert.assertEquals(((short) (0)), calculator.getShort());" + nl +
            "    org.junit.Assert.assertEquals(0, calculator.getCeiling());" + nl +
            "    org.junit.Assert.assertEquals(0L, calculator.getSquare());" + nl +
            "    // AssertGenerator replace invocation" + nl +
            "    boolean o_Integraltypestest_cf1237__11 = // StatementAdderMethod cloned existing statement" + nl +
            "calculator.isOdd();" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(o_Integraltypestest_cf1237__11);" + nl +
            "    org.junit.Assert.assertEquals(0, calculator.getLastOperatorSymbol());" + nl +
            "}";

    @Test
    public void testInjectionOfDescartesIfNeeded() throws Exception {
        final String pathname = "target/trash/pom.xml";
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
            fail("should not throw");
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
            "  <dependency><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.1.11</version></dependency></dependencies>" + nl  +
            "<build><plugins><plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.1.11</version><configuration><mutationEngine>descartes</mutationEngine><mutators><mutator>null</mutator><mutator>void</mutator><mutator>0</mutator><mutator>false</mutator></mutators></configuration><dependencies><dependency><groupId>fr.inria.stamp</groupId><artifactId>descartes</artifactId><version>0.1-SNAPSHOT</version></dependency></dependencies></plugin></plugins></build></project>";


}
