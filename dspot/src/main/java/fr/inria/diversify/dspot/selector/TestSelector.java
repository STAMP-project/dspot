package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.minimization.Minimizer;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public interface TestSelector {

    void init(InputConfiguration configuration);

    List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified);

    List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept);

    void report();

    List<CtMethod<?>> getAmplifiedTestCases();

    Minimizer getMinimizer();

}
