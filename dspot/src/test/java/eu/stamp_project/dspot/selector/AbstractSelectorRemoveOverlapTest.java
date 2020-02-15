package eu.stamp_project.dspot.selector;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.amplifiers.StringLiteralAmplifier;
import eu.stamp_project.dspot.amplifier.amplifiers.value.ValueCreator;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.configuration.DSpotCache;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.amplifier.amplifiers.utils.RandomHelper;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.compilation.TestCompiler;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.InitializeDSpot;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import eu.stamp_project.dspot.common.configuration.options.InputAmplDistributorEnum;
import eu.stamp_project.dspot.common.report.output.Output;
import eu.stamp_project.dspot.common.configuration.test_finder.TestFinder;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/03/18
 */
public abstract class AbstractSelectorRemoveOverlapTest {

    protected String getPathToAbsoluteProjectRoot() {
        return new File("src/test/resources/regression/test-projects_2/").getAbsolutePath();
    }

    protected CtMethod<?> getTest() {
        return this.factory.Class().get("example.TestSuiteExample").getMethodsByName("test2").get(0);
    }

    protected CtClass<?> getTestClass() {
        return this.factory.Class().get("example.TestSuiteOverlapExample");
    }

    protected abstract TestSelector getTestSelector();

    protected abstract String getContentReportFile();

    protected TestSelector testSelectorUnderTest;

    protected Factory factory;

    protected UserInput configuration;

    protected AutomaticBuilder builder;

    protected final String outputDirectory = "target/dspot/output";

    protected DSpotCompiler compiler;

    protected TestCompiler testCompiler;

    private static InitializeDSpot initializeDSpot;

    @Before
    public void setUp() {
        DSpotState.verbose = true;
        this.configuration = new UserInput();
        this.configuration.setAbsolutePathToProjectRoot(getPathToAbsoluteProjectRoot());
        this.configuration.setOutputDirectory(outputDirectory);
        this.builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        this.initializeDSpot = new InitializeDSpot();
        String dependencies = initializeDSpot.completeDependencies(configuration, this.builder);
        DSpotUtils.init(false, outputDirectory,
                this.configuration.getFullClassPathWithExtraDependencies(),
                this.getPathToAbsoluteProjectRoot()
        );
        this.compiler = DSpotCompiler.createDSpotCompiler(
                configuration,
                dependencies
        );
        DSpotCache.init(10000);
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(this.getPathToAbsoluteProjectRoot());
        launcher.buildModel();
        this.factory = launcher.getFactory();
        TestFramework.init(this.factory);
        this.testCompiler = new TestCompiler(
                0, false,
                this.getPathToAbsoluteProjectRoot(),
                this.configuration.getClasspathClassesProject(),
                10000,
                "",
                false
        );
        AssertionGeneratorUtils.init(false);
        DSpotPOMCreator.createNewPom(configuration);
        RandomHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
        this.testSelectorUnderTest = this.getTestSelector();
    }

    @Test
    public void testRemoveOverlappingTests() {
        this.testSelectorUnderTest.init();
        DSpotState dspotState = new DSpotState();
        dspotState.setDelta(0.1f);
        dspotState.setTestFinder(new TestFinder(Collections.emptyList(), Collections.emptyList()));
        dspotState.setCompiler(compiler);
        dspotState.setTestSelector(this.testSelectorUnderTest);
        dspotState.setInputAmplDistributor(InputAmplDistributorEnum.RandomInputAmplDistributor.getInputAmplDistributor(
                200, Collections.singletonList(new StringLiteralAmplifier())));
        dspotState.setOutput(new Output(getPathToAbsoluteProjectRoot(), configuration.getOutputDirectory(), null));
        dspotState.setNbIteration(1);
        dspotState.setAutomaticBuilder(builder);
        dspotState.setTestCompiler(testCompiler);
        dspotState.setTestClassesToBeAmplified(Collections.singletonList(getTestClass()));
        dspotState.setAssertionGenerator(new AssertionGenerator(0.1f, compiler, testCompiler));
        DSpot dspot = new DSpot(dspotState);
        dspot.run();
        final File directory = new File(DSpotUtils.shouldAddSeparator.apply(this.configuration.getOutputDirectory()));
        if (!directory.exists()) {
            directory.mkdir();
        }
        assertEquals(getContentReportFile(), this.testSelectorUnderTest.report().output(this.getTestClass(), this.outputDirectory));
    }
}
