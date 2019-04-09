package eu.stamp_project.prettifier.output.report.minimization.general;

import eu.stamp_project.prettifier.output.report.minimization.MinimizationOfAssertionsJSON;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/02/19
 */
public class GeneralMinimizationJSON {

    public InlineLocalVariablesJSON inlineLocalVariables;

    public MinimizationOfAssertionsJSON removeRedundantAssertions;

    public double medianTimeMinimizationInMillis;

    public GeneralMinimizationJSON() {
        this.inlineLocalVariables = new InlineLocalVariablesJSON();
        this.removeRedundantAssertions = new MinimizationOfAssertionsJSON();
    }

}
