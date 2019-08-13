package eu.stamp_project.prettifier.minimization;

import eu.stamp_project.prettifier.output.report.ReportJSON;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public interface Minimizer {

    /**
     * this method aims to remove all useless statement according to a given test criterion
     * @param amplifiedTestToBeMinimized the amplified test method to be minimized by this minimizer
     * @return a minimized version of amplifiedTestToBeMinimized
     */
    CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized);

    /**
     * This method update the internal report of the minimizer
     * @param report the json report that contains information about the minimization, e.g. time to minimized etc.
     */
    void updateReport(ReportJSON report);
}
