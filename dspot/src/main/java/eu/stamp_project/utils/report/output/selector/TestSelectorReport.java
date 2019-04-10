package eu.stamp_project.utils.report.output.selector;

import eu.stamp_project.utils.report.Report;
import spoon.reflect.declaration.CtType;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public interface TestSelectorReport extends Report {

    public void addTestSelectorReportForTestClass(CtType<?> testClass, TestSelectorElementReport report);

}
