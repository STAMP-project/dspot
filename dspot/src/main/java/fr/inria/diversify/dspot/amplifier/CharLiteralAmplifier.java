package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtLiteral;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CharLiteralAmplifier extends AbstractLiteralAmplifier<Character> {

    @Override
    protected Set<Character> amplify(CtLiteral<Character> existingLiteral) {
        final Character value = existingLiteral.getValue();
        return Stream.of('\0', ' ', AmplificationHelper.getRandomChar(),
                (char) (value + 1),
                (char) (value - 1),
                System.getProperty("line.separator").charAt(0)
        ).collect(Collectors.toSet());
    }

    @Override
    protected String getSuffix() {
        return "litChar";
    }

    @Override
    protected Class<?> getTargetedClass() {
        return Character.class;
    }
}
