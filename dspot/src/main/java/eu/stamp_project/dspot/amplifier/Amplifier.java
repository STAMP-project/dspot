package eu.stamp_project.dspot.amplifier;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 *
 * Amplifier define a way to amplify the given test method.
 *
 */
public interface Amplifier {

    Stream<CtMethod<?>> apply(CtMethod<?> testMethod);

    void reset(CtType<?> testClass);

}
