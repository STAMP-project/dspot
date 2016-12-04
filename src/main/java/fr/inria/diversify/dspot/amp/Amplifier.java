package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.log.branch.Coverage;
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
    CtMethod applyRandom(CtMethod testMethod);

    void reset(Coverage coverage, CtType testClass);

}
