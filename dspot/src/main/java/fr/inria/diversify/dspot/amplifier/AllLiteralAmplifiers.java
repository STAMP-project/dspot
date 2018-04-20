package fr.inria.diversify.dspot.amplifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/03/18
 */
public class AllLiteralAmplifiers implements Amplifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllLiteralAmplifiers.class);

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
    public List<CtMethod> apply(CtMethod testMethod) {
        return this.literalAmplifiers.stream()
                .flatMap(amplifier -> amplifier.apply(testMethod).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void reset(CtType testClass) {
        this.literalAmplifiers.forEach(amplifier -> amplifier.reset(testClass));
    }
}
