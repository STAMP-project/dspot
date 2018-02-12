package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StringLiteralAmplifier extends AbstractLiteralAmplifier<String> {

    @Override
    protected Set<CtLiteral<String>> amplify(CtLiteral<String> existingLiteral) {
        final Factory factory = existingLiteral.getFactory();
        Set<CtLiteral<String>> values = new HashSet<>();
        String value = (String) existingLiteral.getValue();
        // TODO idk if we should add all values around

        values.addAll(this.testClassToBeAmplified.getElements(new TypeFilter<CtLiteral<String>>(CtLiteral.class))
                .stream()
                .filter(element -> element.getValue() instanceof String &&
                        element.getValue() != null &&
                        !element.equals(existingLiteral))
                .map(CtLiteral::clone)
                .collect(Collectors.toList()));
        if (value != null) {
            if (value.length() > 2) {
                int length = value.length();
                // add one random char
                int index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
                values.add(factory.createLiteral(value.substring(0, index - 1) + AmplificationHelper.getRandomChar() + value.substring(index, length)));

                // replace one random char
                index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
                values.add(factory.createLiteral(value.substring(0, index) + AmplificationHelper.getRandomChar() + value.substring(index, length)));

                // remove one random char
                index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
                values.add(factory.createLiteral(value.substring(0, index) + value.substring(index + 1, length)));
            } else {
                values.add(factory.createLiteral("" + AmplificationHelper.getRandomChar()));
            }
        }
        // add one random string
        values.add(factory.createLiteral(AmplificationHelper.getRandomString(10)));
        // add empty string
        values.add(factory.createLiteral(""));
        return values;
    }

    @Override
    protected String getSuffix() {
        return "litString";
    }

    @Override
    protected Class<?> getTargetedClass() {
        return String.class;
    }
}
