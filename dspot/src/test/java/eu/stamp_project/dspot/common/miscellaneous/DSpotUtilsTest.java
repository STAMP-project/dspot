package eu.stamp_project.dspot.common.miscellaneous;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.collector.NullCollector;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/20/17
 */
public class DSpotUtilsTest extends AbstractTestOnSample {

    private final static File outputDirectory = new File("target/trash/");

    @Test
    public void testOutputUsingToString() throws Exception {
        DSpotUtils.printCtTypUsingToStringToGivenDirectory(
                launcher.getFactory().Class().get("fr.inria.lombok.LombokClassThatUseBuilderTest"),
                outputDirectory
        );
        try (final BufferedReader reader =
                     new BufferedReader(new FileReader(outputDirectory + "/fr/inria/lombok/LombokClassThatUseBuilderTest.java"))) {
            assertTrue(reader.lines()
                    .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)).endsWith(
                            "public class LombokClassThatUseBuilderTest {" + AmplificationHelper.LINE_SEPARATOR +
                                    "    @org.junit.Test" + AmplificationHelper.LINE_SEPARATOR +
                                    "    public void test() {" + AmplificationHelper.LINE_SEPARATOR +
                                    "        fr.inria.lombok.LombokClassThatUseBuilder.builder().build();" + AmplificationHelper.LINE_SEPARATOR +
                                    "    }" + AmplificationHelper.LINE_SEPARATOR +
                                    "}"
                    )
            );
        }
    }

    @Test
    public void testWithLombokAnnotation() throws Exception {
        DSpotUtils.printAndCompileToCheck(
                findClass("fr.inria.lombok.LombokClassThatUseBuilderTest"),
                outputDirectory,
                new NullCollector()
        );
        try (final BufferedReader reader =
                     new BufferedReader(new FileReader(outputDirectory + "/fr/inria/lombok/LombokClassThatUseBuilderTest.java"))) {
            assertEquals(
            "package fr.inria.lombok;" + AmplificationHelper.LINE_SEPARATOR +
                    "" + AmplificationHelper.LINE_SEPARATOR +
                    "" + AmplificationHelper.LINE_SEPARATOR +
                    "import org.junit.Test;" + AmplificationHelper.LINE_SEPARATOR +
                    "" + AmplificationHelper.LINE_SEPARATOR +
                    "" + AmplificationHelper.LINE_SEPARATOR +
                    "public class LombokClassThatUseBuilderTest {" + AmplificationHelper.LINE_SEPARATOR +
                    "    @Test" + AmplificationHelper.LINE_SEPARATOR +
                    "    public void test() {" + AmplificationHelper.LINE_SEPARATOR +
                    "        builder().build();" + AmplificationHelper.LINE_SEPARATOR +
                    "    }" + AmplificationHelper.LINE_SEPARATOR +
                    "}" + AmplificationHelper.LINE_SEPARATOR,
                    reader.lines()
                            .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
            );
        }
    }

    @Test
    public void testGetAllTestClasses() {

        /*
            Test the method getAllTestClasses.
                This method should return an array of all the test classes, i.e. class that contains at least one test method.
                This array should not contain any test class that has been excluded, see UserInput#excludedClasses
         */

//        final String[] allTestClasses = DSpotUtils.getAllTestClasses();
        //       assertEquals(33, allTestClasses.length); // we got all
        //       assertTrue(Arrays.stream(allTestClasses).noneMatch(s -> s.startsWith("fr.inria.filter.failing."))); // but not excluded
    }

    @Test
    public void testPrintAmplifiedTestClass() throws Exception {
        final File javaFile = new File(outputDirectory.getAbsolutePath() + "/" + "example.TestSuiteExample".replaceAll("\\.", "\\/") + ".java");
        try {
            FileUtils.forceDelete(javaFile);
        } catch (IOException ignored) {
            //ignored
        }

        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/resources/test-projects/src/test/java/example/TestSuiteExample.java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        final CtType<?> type = launcher.getFactory().Type().get("example.TestSuiteExample");

        assertFalse(javaFile.exists());
        DSpotUtils.printAndCompileToCheck(type, outputDirectory, new NullCollector());
        assertTrue(javaFile.exists());

        final CtMethod<?> clone = type.getMethods().stream()
                .findFirst().get().clone();
        final int nbMethodStart = type.getMethods().size();
        type.getMethods().forEach(type::removeMethod);
        clone.setSimpleName("MyNewMethod");
        type.addMethod(clone);

        DSpotUtils.printAndCompileToCheck(type, outputDirectory, new NullCollector());
        launcher = new Launcher();
        launcher.addInputResource(outputDirectory.getAbsolutePath() + "/" + "example.TestSuiteExample".replaceAll("\\.", "\\/") + ".java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();

        assertEquals(nbMethodStart + 1, launcher.getFactory().Class().get("example.TestSuiteExample").getMethods().size());

        type.getMethods().forEach(type::removeMethod);
        clone.setSimpleName("MyNewMethod2");
        type.addMethod(clone);

        DSpotUtils.printAndCompileToCheck(type, outputDirectory, new NullCollector());
        launcher = new Launcher();
        launcher.addInputResource(outputDirectory.getAbsolutePath() + "/" + "example.TestSuiteExample".replaceAll("\\.", "\\/") + ".java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();

        assertEquals(nbMethodStart + 2, launcher.getFactory().Class().get("example.TestSuiteExample").getMethods().size());
    }


}
