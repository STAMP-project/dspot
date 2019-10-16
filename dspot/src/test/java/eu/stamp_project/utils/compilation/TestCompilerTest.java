package eu.stamp_project.utils.compilation;

import eu.stamp_project.Main;
import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.options.AutomaticBuilderEnum;
import eu.stamp_project.utils.program.InputConfiguration;
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
        final InputConfiguration configuration = new InputConfiguration();
        configuration.setAbsolutePathToProjectRoot(getPathToProjectRoot());
        final AutomaticBuilder builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        String dependencies = Main.completeDependencies(configuration, builder);
        configuration.setDependencies(dependencies);
        DSpotCompiler compiler = DSpotCompiler.createDSpotCompiler(configuration, dependencies);
        CtClass<?> testClass = findClass("fr.inria.filter.failing.FailingTest");
        final ArrayList<CtMethod<?>> methods = new ArrayList<>();
        methods.add(findMethod(testClass, "testAssertionError"));
        methods.add(findMethod(testClass, "testFailingWithException"));
        DSpotUtils.init(false, "target/dspot/",
                configuration.getFullClassPathWithExtraDependencies(), getPathToProjectRoot()
        );
        TestCompiler.init(
                0,
                false,
                getPathToProjectRoot(),
                configuration.getClasspathClassesProject(),
                10000,
                "",
                false
        );
        assertTrue(TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
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
                TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                        testClass,
                        methods,
                        compiler
                ).size()
        );
    }
}
