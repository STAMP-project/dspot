package eu.stamp_project.prettifier.output.report.minimization;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/02/19
 */
public class GlobalMinimizationJSON {

    public InlineLocalVariablesJSON inlineLocalVariables;

    public RemoveRedundantAssertions removeRedundantAssertions;

    public double medianTimeMinimizationInMillis;

    public GlobalMinimizationJSON() {
        this.inlineLocalVariables = new InlineLocalVariablesJSON();
        this.removeRedundantAssertions = new RemoveRedundantAssertions();
    }

}
