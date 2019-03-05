package eu.stamp_project.prettifier.output.report;

import eu.stamp_project.prettifier.output.report.minimization.MinimizationOfAssertionsJSON;
import eu.stamp_project.prettifier.output.report.minimization.general.GeneralMinimizationJSON;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/02/19
 */
public class ReportJSON {

    public GeneralMinimizationJSON generalMinimizationJSON;

    public MinimizationOfAssertionsJSON pitMinimizationJSON;

    public int nbTestMethods;

    public double medianNbStatementBefore;

    public double medianNbStatementAfter;

    public ReportJSON() {
        this.generalMinimizationJSON = new GeneralMinimizationJSON();
        this.pitMinimizationJSON = new MinimizationOfAssertionsJSON();
    }

}
