package eu.stamp_project.minimization;

import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public interface Minimizer {

    /**
     * this method aims to remove all useless statement according to a given test criterion
     * @param amplifiedTestToBeMinimized
     * @return a minimized version of amplifiedTestToBeMinimized
     */
    CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized);

}
