package eu.stamp_project.dspot.amplifier;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public abstract class DecoratorAmplifier implements Amplifier {

    private Amplifier internalAmplifier;

    public DecoratorAmplifier(Amplifier internalAmplifier) {
        this.internalAmplifier = internalAmplifier;
    }

    @Override
    public Stream<CtMethod<?>> amplify(CtMethod testMethod, int iteration) {
        return this.internalAmplifier.amplify(testMethod, 0);
    }

    @Override
    public void reset(CtType testClass) {
        this.internalAmplifier.reset(testClass);
    }
}
