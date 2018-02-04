package fr.inria.diversify.dspot.amplifier;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 *
 * Amplifier define a way to amplify the given test method.
 *
 */
public interface Amplifier {

    List<CtMethod> apply(CtMethod testMethod);

    void reset(CtType testClass);

}
