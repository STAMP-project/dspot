package eu.stamp_project.dspot.amplifier;

import spoon.reflect.declaration.CtMethod;

import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public class IterationDecoratorAmplifier extends DecoratorAmplifier {

    private int currentIteration;
    private int frequency;

    /**
     * This Amplifier is meant to decorate an Amplifier.
     * This decorator will apply the decorated Amplifier each time specified by the frequency fields
     * @param internalAmplifier the amplifier to be decorated
     * @param frequency the frequency to apply the decorated amplifier,
     *                  <i>e.g.</i> when frequency = 2, It will apply the amplifier each 2 iterations.
     */
    public IterationDecoratorAmplifier(Amplifier internalAmplifier, int frequency) {
        super(internalAmplifier);
        this.frequency = frequency;
        this.currentIteration = 0;
    }

    @Override
    public Stream<CtMethod<?>> apply(CtMethod testMethod) {
        if (this.currentIteration++ % this.frequency == 0) {
            return super.apply(testMethod);
        } else {
            return Stream.empty();
        }
    }
}
