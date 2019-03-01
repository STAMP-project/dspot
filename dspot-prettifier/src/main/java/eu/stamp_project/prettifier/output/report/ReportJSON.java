package eu.stamp_project.prettifier.output.report;

import eu.stamp_project.prettifier.output.report.minimization.GlobalMinimizationJSON;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/02/19
 */
public class ReportJSON {

    public GlobalMinimizationJSON globalMinimization;

    public int nbTestMethods;

    public double medianNbStatementBefore;

    public double medianNbStatementAfter;

    public ReportJSON() {
        this.globalMinimization = new GlobalMinimizationJSON();
    }

}
