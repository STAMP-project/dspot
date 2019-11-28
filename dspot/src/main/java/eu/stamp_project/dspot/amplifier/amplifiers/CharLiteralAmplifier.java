package eu.stamp_project.dspot.amplifier.amplifiers;

import eu.stamp_project.dspot.amplifier.RandomHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import java.util.HashSet;
import java.util.Set;

public class CharLiteralAmplifier extends AbstractLiteralAmplifier<Character> {

    @Override
    protected Set<CtExpression<Character>> amplify(CtExpression<Character> original, CtMethod<?> testMethod) {
        final Set<CtExpression<Character>> values = new HashSet<>();
        final Factory factory = testMethod.getFactory();
        values.add(factory.createLiteral('\0'));
        values.add(factory.createLiteral(' '));
        values.add(factory.createLiteral(RandomHelper.getRandomChar()));
        values.add(factory.createLiteral(System.getProperty("line.separator").charAt(0)));
        if (((CtLiteral<Character>)original).getValue() != null){
            final Character value = ((CtLiteral<Character>)original).getValue();
            values.add(factory.createLiteral((char) (value + 1)));
            values.add(factory.createLiteral((char) (value - 1)));
        }
        return values;
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
