package eu.stamp_project.dspot.amplifier;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/03/18
 */
public class AllLiteralAmplifiers implements Amplifier {

    private List<Amplifier> literalAmplifiers;

    public AllLiteralAmplifiers() {
        this.literalAmplifiers = Arrays.asList(
                new StringLiteralAmplifier(),
                new NumberLiteralAmplifier(),
                new BooleanLiteralAmplifier(),
                new CharLiteralAmplifier()
        );
    }

    @Override
    public Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration) {
        return this.literalAmplifiers.stream().flatMap(amplifier -> amplifier.amplify(testMethod, 0));
    }

    @Override
    public void reset(CtType testClass) {
        this.literalAmplifiers.forEach(amplifier -> amplifier.reset(testClass));
    }
}
