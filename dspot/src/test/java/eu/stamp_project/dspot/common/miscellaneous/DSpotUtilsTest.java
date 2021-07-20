package eu.stamp_project.dspot.common.miscellaneous;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.compilation.TestCompiler;
import eu.stamp_project.dspot.common.configuration.InitializeDSpot;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.collector.NullCollector;
import eu.stamp_project.dspot.selector.TestSelector;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;

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

    private UserInput configuration;

    private AutomaticBuilder builder;

    private DSpotCompiler compiler;

    private Factory factory;

    private TestSelector testSelector;

    private TestCompiler testCompiler;

    private static InitializeDSpot initializeDSpot;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        super.setUp();
        this.configuration = new UserInput();
        this.configuration.setAbsolutePathToProjectRoot(getPathToProjectRoot());
        this.configuration.setOutputDirectory(outputDirectory.getAbsolutePath());
        this.builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        this.initializeDSpot = new InitializeDSpot();
        String dependencies = initializeDSpot.completeDependencies(configuration, this.builder);
        DSpotUtils.init(CommentEnum.None,
                outputDirectory.getAbsolutePath(),
                this.configuration.getFullClassPathWithExtraDependencies(),
                this.getPathToProjectRoot()
        );
        this.compiler = DSpotCompiler.createDSpotCompiler(
                configuration,
                dependencies
        );
    }

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
                    "import org.junit.Test;" + AmplificationHelper.LINE_SEPARATOR +
                    "public class LombokClassThatUseBuilderTest {" + AmplificationHelper.LINE_SEPARATOR +
                    "    @Test" + AmplificationHelper.LINE_SEPARATOR +
                    "    public void test() {" + AmplificationHelper.LINE_SEPARATOR +
                    "        LombokClassThatUseBuilder.builder().build();" + AmplificationHelper.LINE_SEPARATOR +
                    "    }" + AmplificationHelper.LINE_SEPARATOR +
                    "}",
                    reader.lines()
                            .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
            );
        }
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

    @Test
    public void test() {

        /* Testing scenario:
            Build a model with a class
            Clone this class
            Modify the clone by adding a method
            Print out the clone
            Build a model using the printed clone
            Compare the number of number between the outputted class and the cloned one
         */

        // build original class
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/java/eu/stamp_project/dspot/common/miscellaneous/DSpotUtilsTest.java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        final Factory factory = launcher.getFactory();

        // clone
        final CtClass<?> compilationUnitPrintTest = factory.Class().get("eu.stamp_project.dspot.common.miscellaneous.DSpotUtilsTest");
        final CtClass<?> clone = compilationUnitPrintTest.clone();
        compilationUnitPrintTest.getPackage().addType(clone);
        assertEquals(5 , clone.getMethods().size());
        assertEquals(5 , compilationUnitPrintTest.getMethods().size());

        // modification
        CtMethod<?> cloneMethod = ((CtMethod<?>) clone.getMethodsByName("test").get(0)).clone();
        cloneMethod.setSimpleName("cloneTest");
        clone.addMethod(cloneMethod);
        assertEquals(6 , clone.getMethods().size());
        assertEquals(5 , compilationUnitPrintTest.getMethods().size());

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File("target"));

        assertEquals(6 , clone.getMethods().size());
        assertEquals(5 , compilationUnitPrintTest.getMethods().size());

        // building now a new model from the java file outputted just before
        launcher = new Launcher();
        launcher.addInputResource("target/eu/stamp_project/dspot/common/miscellaneous/DSpotUtilsTest.java");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();

        // compare the number of methods in the printed class and the clone, should be the same (2)
        assertEquals(
                clone.getMethods().size(),
                launcher.getFactory().Class().get("eu.stamp_project.dspot.common.miscellaneous.DSpotUtilsTest").getMethods().size()
        );

    }
}
