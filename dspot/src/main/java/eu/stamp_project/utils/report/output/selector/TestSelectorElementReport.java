package eu.stamp_project.utils.report.output.selector;

import spoon.reflect.declaration.CtType;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/04/19
 */
public interface TestSelectorElementReport {

    public String output(CtType<?> testClass);

}
