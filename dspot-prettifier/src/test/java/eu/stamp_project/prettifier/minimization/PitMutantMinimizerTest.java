package eu.stamp_project.prettifier.minimization;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.pit.AbstractPitResult;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/03/19
 */
public class PitMutantMinimizerTest extends AbstractTest {

    CtClass<?> testClass;
    CtMethod<?> testMethod;
    PitMutantMinimizer minimizer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.testClass = Utils.findClass("eu.stamp_project.AppTest");
        this.testMethod = Utils.findMethod(testClass, "test1");
        this.minimizer = new PitMutantMinimizer(
                testClass,
                this.configuration.getBuilderEnum().getAutomaticBuilder(this.configuration),
                this.configuration.getAbsolutePathToProjectRoot(),
                this.configuration.getClasspathClassesProject(),
                this.configuration.getAbsolutePathToTestClasses()
        );
    }

    @Test
    public void test() {

        /*
               Test the minimization
         */

        final CtMethod<?> minimize = minimizer.minimize(testMethod);
        System.out.println(minimize);
        assertEquals(4, testMethod.getElements(TestFramework.ASSERTIONS_FILTER).size());
        assertEquals(1, minimize.getElements(TestFramework.ASSERTIONS_FILTER).size());
        assertEquals(8, testMethod.getBody().getStatements().size());
        assertEquals(4, minimize.getBody().getStatements().size());
    }

    @Test
    public void testPrintCompileAndRunPit() {
        /*
            Test that the Minimizer is able to print, compile and run PIT
         */

        final List<AbstractPitResult> abstractPitResults = minimizer.printCompileAndRunPit(testClass);
        assertEquals(12, abstractPitResults.size());
        System.out.println(abstractPitResults);
    }

    @Test
    public void testRemoveCloneAndInsert() {
        /*
            Test the method removeCloneAndInsert()
                This method should let the method given in parameter the same
                The returned method should be a clone of the given method, minus one assertions
         */

        assertEquals(8, testMethod.getBody().getStatements().size());

        final List<CtInvocation<?>> assertions =
                testMethod.getElements(TestFramework.ASSERTIONS_FILTER);
        final String beforeString = testMethod.toString();
        CtMethod<?> ctMethod = minimizer.removeCloneAndInsert(assertions, testMethod, 0);
        String afterString = testMethod.toString();
        assertEquals(beforeString, afterString);
        assertEquals(7, ctMethod.getBody().getStatements().size());
        assertEquals(8, testMethod.getBody().getStatements().size());

        ctMethod = minimizer.removeCloneAndInsert(assertions, testMethod, 1);
        afterString = testMethod.toString();
        assertEquals(beforeString, afterString);
        assertEquals(7, ctMethod.getBody().getStatements().size());
        assertEquals(8, testMethod.getBody().getStatements().size());

        ctMethod = minimizer.removeCloneAndInsert(assertions, testMethod, 2);
        afterString = testMethod.toString();
        assertEquals(beforeString, afterString);
        assertEquals(7, ctMethod.getBody().getStatements().size());
        assertEquals(8, testMethod.getBody().getStatements().size());
    }
}
