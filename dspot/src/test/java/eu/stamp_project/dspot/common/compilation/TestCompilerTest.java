package eu.stamp_project.dspot.common.compilation;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.configuration.InitializeDSpot;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/13/17
 */
public class TestCompilerTest extends AbstractTestOnSample {

    @Test
    public void test() {
        final UserInput configuration = new UserInput();
        configuration.setAbsolutePathToProjectRoot(getPathToProjectRoot());
        final AutomaticBuilder builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        InitializeDSpot initializeDSpot = new InitializeDSpot();
        String dependencies = initializeDSpot.completeDependencies(configuration, builder);
        configuration.setDependencies(dependencies);
        DSpotCompiler compiler = DSpotCompiler.createDSpotCompiler(configuration, dependencies);
        CtClass<?> testClass = findClass("fr.inria.filter.failing.FailingTest");
        final ArrayList<CtMethod<?>> methods = new ArrayList<>();
        methods.add(findMethod(testClass, "testAssertionError"));
        methods.add(findMethod(testClass, "testFailingWithException"));
        DSpotUtils.init(CommentEnum.None, "target/dspot/",
                configuration.getFullClassPathWithExtraDependencies(), getPathToProjectRoot()
        );
        TestCompiler testCompiler = new TestCompiler(
                0,
                false,
                getPathToProjectRoot(),
                configuration.getClasspathClassesProject(),
                10000,
                "",
                false
        );
        assertTrue(testCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                testClass,
                methods,
                compiler
        ).isEmpty());

        testClass = findClass("fr.inria.filter.passing.PassingTest");
        methods.clear();
        methods.add(findMethod(testClass, "testAssertion"));
        methods.add(findMethod(testClass, "testNPEExpected"));
        methods.add(findMethod(testClass, "failingTestCase"));
        assertEquals(2,
                testCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                        testClass,
                        methods,
                        compiler
                ).size()
        );
    }
}
