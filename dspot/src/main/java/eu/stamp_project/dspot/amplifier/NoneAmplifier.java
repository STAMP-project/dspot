package eu.stamp_project.dspot.amplifier;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/09/19
 */
public class NoneAmplifier implements Amplifier {

    @Override
    public Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration) {
        return Stream.empty();
    }

    @Override
    public void reset(CtType<?> testClass) {

    }
}
