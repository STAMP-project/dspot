package fr.inria.diversify.dspot.testselector;

import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/13/16
 */
public interface TestSelector {

    void init();

    List<CtMethod> select(List<CtMethod> methodTest);

    void update();

}
