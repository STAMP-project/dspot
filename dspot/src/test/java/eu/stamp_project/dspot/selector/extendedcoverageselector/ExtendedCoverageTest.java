package eu.stamp_project.dspot.selector.extendedcoverageselector;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedCoverageTest {

    String EXECUTION_PATH_1 = "package.package.class:method1+()V+1,0,3,0|method2+(LCallback;)V+0,4" +
            "-package.package.class2:method1+()V+4,3,0";
    String EXECUTION_PATH_2 = "package.package.class:method1+()V+1,0,3,5|method2+(LCallback;)V+0,1" +
            "-package.package.class2:method1+()V+4,3,0";
    String EXECUTION_PATH_3 = "package.package.class:method1+()V+1,0,3,5|method2+(LCallback;)V+0,4" +
            "-package.package.class2:method1+()V+4,3,0";

    private ExtendedCoverage genExtendedCoverageWithPath(String executionPath) {
        Coverage coverage = new CoverageImpl(8, 10);
        coverage.setExecutionPath(executionPath);

        return new ExtendedCoverage(coverage);
    }

    @Test
    public void constructExtendedCoverageFromCoverage() {
        ExtendedCoverage extendedCoverage = genExtendedCoverageWithPath(EXECUTION_PATH_1);

        ProjectCoverageMap instructionsCoveredPerClass = new ProjectCoverageMap();
        ClassCoverageMap instructionsCoveredPerMethod = new ClassCoverageMap();
        instructionsCoveredPerMethod.addMethodCoverage("method1", new MethodCoverage(Arrays.asList(1, 0, 3, 0), "()V"));
        instructionsCoveredPerMethod.addMethodCoverage("method2", new MethodCoverage(Arrays.asList(0, 4), "(LCallback;)V"));
        instructionsCoveredPerClass.addClassCoverage("package.package.class", instructionsCoveredPerMethod);
        ClassCoverageMap instructionsCoveredPerMethod2 = new ClassCoverageMap();
        instructionsCoveredPerMethod2.addMethodCoverage("method1", new MethodCoverage(Arrays.asList(4, 3, 0),
                "()V"));
        instructionsCoveredPerClass.addClassCoverage("package.package.class2", instructionsCoveredPerMethod2);

        Assert.assertEquals(extendedCoverage.getInstructionsProjectCoverageMap(), instructionsCoveredPerClass);
    }

    @Test
    public void testAccumulate() {
        ExtendedCoverage extendedCoverage = genExtendedCoverageWithPath(EXECUTION_PATH_1);
        ExtendedCoverage extendedCoverage2 = genExtendedCoverageWithPath(EXECUTION_PATH_2);

        extendedCoverage.accumulate(extendedCoverage2);

        Assert.assertEquals(extendedCoverage, genExtendedCoverageWithPath(EXECUTION_PATH_3));
    }

    @Test
    public void testBetterThan() {
        ExtendedCoverage extendedCoverage = genExtendedCoverageWithPath(EXECUTION_PATH_1);
        ExtendedCoverage extendedCoverage2 = genExtendedCoverageWithPath(EXECUTION_PATH_2);

        Assert.assertTrue(extendedCoverage.isBetterThan(extendedCoverage2));
        Assert.assertTrue(extendedCoverage2.isBetterThan(extendedCoverage));
    }

    @Test
    public void testBetterThanNotReflexive() {
        ExtendedCoverage extendedCoverage = genExtendedCoverageWithPath(EXECUTION_PATH_1);

        Assert.assertFalse(extendedCoverage.isBetterThan(extendedCoverage));
    }

}
