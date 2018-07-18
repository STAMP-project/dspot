package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationHelper;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CharLiteralAmplifier extends AbstractLiteralAmplifier<Character> {

    @Override
    protected Set<CtLiteral<Character>> amplify(CtLiteral<Character> original, CtMethod<?> testMethod) {
        final Character value = original.getValue();
        final Factory factory = testMethod.getFactory();
        return Stream.of(
                factory.createLiteral('\0'),
                factory.createLiteral(' '),
                factory.createLiteral(AmplificationHelper.getRandomChar()),
                factory.createLiteral((char) (value + 1)),
                factory.createLiteral((char) (value - 1)),
                factory.createLiteral(System.getProperty("line.separator").charAt(0))
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
