package eu.stamp_project.dspot.budget;

import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public interface Budgetizer {

    List<CtMethod<?>> inputAmplify(List<CtMethod<?>> testMethods, int iteration);

}
