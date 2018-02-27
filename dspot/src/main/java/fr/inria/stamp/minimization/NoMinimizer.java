package fr.inria.stamp.minimization;

import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/02/18
 */
public class NoMinimizer implements Minimizer{

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        return amplifiedTestToBeMinimized;
    }

}
