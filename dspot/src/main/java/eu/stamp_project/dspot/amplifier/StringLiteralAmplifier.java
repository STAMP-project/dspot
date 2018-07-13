package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationHelper;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StringLiteralAmplifier extends AbstractLiteralAmplifier<String> {

    private List<String> existingStrings;

    private boolean hasBeenApplied;

    public StringLiteralAmplifier() {
        this.existingStrings = new ArrayList<>();
        this.hasBeenApplied = false;
    }

    @Override
    public Stream<CtMethod<?>> apply(CtMethod<?> testMethod) {
        flatStringLiterals(testMethod);
        List<CtLiteral<String>> literals = testMethod.getElements(LITERAL_TYPE_FILTER);
        if (literals.isEmpty()) {
            return Stream.empty();
        }
        // here, we call a termination function, i.e. collect
        // because Stream are lazy, we need this to manage hasBeenApplied state to avoir
        // redundant amplification
        final List<Stream<String>> newValues =
                literals.stream()
                        .map(literal -> this.amplify(literal).stream())
                        .collect(Collectors.toList());
        this.hasBeenApplied = true;
        return IntStream.range(0, literals.size())
                .boxed()
                .flatMap(index ->
                        newValues.get(index)
                                .map(newValue -> replace(literals.get(index), newValue, testMethod))
                );
    }

    static void flatStringLiterals(CtMethod<?> testMethod) {
        final List<CtBinaryOperator> deepestBinOp = testMethod.getElements(
                op -> (op.getLeftHandOperand() instanceof CtLiteral &&
                        ((CtLiteral) op.getLeftHandOperand()).getValue() instanceof String) &&
                        op.getRightHandOperand() instanceof CtLiteral &&
                        ((CtLiteral) op.getRightHandOperand()).getValue() instanceof String
        );
        deepestBinOp.forEach(StringLiteralAmplifier::concatAndReplace);
        if (deepestBinOp.stream()
                .allMatch(ctBinaryOperator ->
                        ctBinaryOperator.getParent(CtBinaryOperator.class) == null)) {
            return;
        } else {
            flatStringLiterals(testMethod);
        }
    }

    static void concatAndReplace(CtBinaryOperator<?> binaryOperator) {
        final CtLiteral<?> ctElement = concatString(binaryOperator);
        binaryOperator.replace(ctElement);
    }

    static CtLiteral<?> concatString(CtBinaryOperator<?> binaryOperator) {
        return binaryOperator.getFactory().createLiteral(
                ((String) ((CtLiteral) binaryOperator.getLeftHandOperand()).getValue())
                        + ((String) ((CtLiteral) binaryOperator.getRightHandOperand()).getValue())
        );
    }

    @Override
    protected Set<String> amplify(CtLiteral<String> existingLiteral) {
        Set<String> values = new HashSet<>();
        values.add(this.existingStrings.get(AmplificationHelper.getRandom().nextInt(this.existingStrings.size() - 1)));
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
                // add one random string
                values.add(AmplificationHelper.getRandomString(value.length()));
            } else {
                values.add("" + AmplificationHelper.getRandomChar());
            }
        }
        if (!this.hasBeenApplied) {
            // add special strings
            values.add("");
            values.add(System.getProperty("line.separator"));
            values.add(System.getProperty("path.separator"));
        }
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
        this.hasBeenApplied = false;
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
