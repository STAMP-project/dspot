package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.runner.InputConfiguration;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public interface TestSelector {

    void init(InputConfiguration configuration);

    @Deprecated
    void reset();

    List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified);

    List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept);

    @Deprecated
    void update();

    void report();

    int getNbAmplifiedTestCase();

    CtType buildClassForSelection(CtType original, List<CtMethod<?>> methods);

}
