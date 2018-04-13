package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StringLiteralAmplifier extends AbstractLiteralAmplifier<String> {

    private List<String> existingStrings;

    public StringLiteralAmplifier() {
        this.existingStrings = new ArrayList<>();
    }

    @Override
    protected Set<String> amplify(CtLiteral<String> existingLiteral) {
        final Factory factory = existingLiteral.getFactory();

        // initialize values with existing strings
        Set<String> values = new HashSet<>(this.existingStrings);
        values.remove(existingLiteral);

        String value = (String) existingLiteral.getValue();
        if (value != null) {
            if (value.length() > 2) {
                int length = value.length();
                // add one random char
                int index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
                values.add(value.substring(0, index - 1) + AmplificationHelper.getRandomChar() + value.substring(index, length));
                // replace one random char
                index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
                values.add(value.substring(0, index) + AmplificationHelper.getRandomChar() + value.substring(index, length));
                // remove one random char
                index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
                values.add(value.substring(0, index) + value.substring(index + 1, length));
            } else {
                values.add("" + AmplificationHelper.getRandomChar());
            }
        }
        // add one random string
        values.add(AmplificationHelper.getRandomString(10));

        // add special strings
        values.add("");
        values.add(System.getProperty("line.separator"));
        values.add(System.getProperty("path.separator"));
        values.add("\0");

        return values;
    }



    @Override
    public void reset(CtType testClass) {
        super.reset(testClass);
        this.existingStrings = this.testClassToBeAmplified.getElements(new TypeFilter<CtLiteral<String>>(CtLiteral.class))
                .stream()
                .filter(element -> element.getValue() != null && element.getValue() instanceof String)
                .map(CtLiteral::clone)
                .map(CtLiteral::getValue)
                .collect(Collectors.toList());
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
