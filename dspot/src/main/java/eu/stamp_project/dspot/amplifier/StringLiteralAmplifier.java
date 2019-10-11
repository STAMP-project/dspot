package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.RandomHelper;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
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
    protected Set<CtExpression<String>> amplify(CtExpression<String> original, CtMethod<?> testMethod) {
        final Factory factory = testMethod.getFactory();
        Set<CtExpression<String>> values = new HashSet<>();
        if (this.existingStrings.size() > 1) { // > 1 to not replace the lit by its own value
            final int bound = this.existingStrings.size() - 1;
            values.add(factory.createLiteral(this.existingStrings.get(RandomHelper.getRandom().nextInt(bound))));
        }
        String value = ((CtLiteral<String>) original).getValue();
        if (value != null) {
            if (value.length() > 2) {
                int length = value.length();
                // add one random char
                int index = RandomHelper.getRandom().nextInt(length - 2) + 1;
                values.add(factory.createLiteral(value.substring(0, index - 1) + RandomHelper.getRandomChar() + value.substring(index, length)));
                // replace one random char
                index = RandomHelper.getRandom().nextInt(length - 2) + 1;
                values.add(factory.createLiteral(value.substring(0, index) + RandomHelper.getRandomChar() + value.substring(index, length)));
                // remove one random char
                index = RandomHelper.getRandom().nextInt(length - 2) + 1;
                values.add(factory.createLiteral(value.substring(0, index) + value.substring(index + 1, length)));
                // add one random string
                values.add(factory.createLiteral(RandomHelper.getRandomString(value.length())));
            } else {
                values.add(factory.createLiteral("" + RandomHelper.getRandomChar()));
            }
        }

        // add special strings
        values.add(factory.createLiteral(""));
        values.add(factory.createLiteral(System.getProperty("line.separator")));
        values.add(factory.createLiteral(System.getProperty("path.separator")));

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

    public static void flatStringLiterals(CtElement testMethod) {
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
}
