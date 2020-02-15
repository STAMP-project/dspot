package eu.stamp_project.dspot.selector;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.InitializeDSpot;
import eu.stamp_project.dspot.common.execution.TestRunner;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import eu.stamp_project.dspot.selector.pitmutantscoreselector.AbstractPitResult;
import eu.stamp_project.dspot.common.configuration.UserInput;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.factory.Factory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/01/19
 */
public class OneTestClassPitScoreMutantSelectorTest {

    private String FULL_QUALIFIED_NAME_TEST_CLASS = "example.TestSuiteExample";

    private AutomaticBuilder builder;

    private UserInput configuration;

    private TestRunner testRunner;

    private static InitializeDSpot initializeDSpot;

    @Before
    public void setUp() {
        DSpotState.verbose = true;
        this.configuration = new UserInput();
        this.configuration.setAbsolutePathToProjectRoot("src/test/resources/test-projects/");
        this.builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        this.initializeDSpot = new InitializeDSpot();
        DSpotCompiler.createDSpotCompiler(
                configuration,
                initializeDSpot.completeDependencies(configuration, this.builder)
        );
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource("src/test/resources/test-projects/");
        launcher.buildModel();
        Factory factory = launcher.getFactory();
        this.configuration.setFactory(factory);
        this.configuration.setTestClasses(Collections.singletonList(FULL_QUALIFIED_NAME_TEST_CLASS));
        this.configuration.setTargetOneTestClass(true);
        this.testRunner = new TestRunner(this.configuration.getAbsolutePathToProjectRoot(), "", false);
    }

    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {
        final PitMutantScoreSelector pitMutantScoreSelector = new PitMutantScoreSelector(this.builder, this.configuration);
        pitMutantScoreSelector.init();
        final Field field = pitMutantScoreSelector.getClass().getDeclaredField("originalKilledMutants");
        field.setAccessible(true);
        List<AbstractPitResult> originalResult = (List<AbstractPitResult>) field.get(pitMutantScoreSelector);
        assertTrue(originalResult.stream().allMatch(abstractPitResult -> abstractPitResult.getFullQualifiedNameOfKiller().equals(FULL_QUALIFIED_NAME_TEST_CLASS)));
    }
}
