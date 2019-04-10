package eu.stamp_project.utils.report.output;

import eu.stamp_project.utils.report.Report;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/04/19
 */
public interface OutputReport extends Report {

    public void addNumberAmplifiedTestMethodsToTotal(int numberOfAmplifiedTestMethods);

    public void addPrintedTestClasses(String line);

}
