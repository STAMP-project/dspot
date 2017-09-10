package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NumberLiteralAmplifier implements Amplifier {

    private final TypeFilter<CtLiteral> literalTypeFilter = new TypeFilter<CtLiteral>(CtLiteral.class) {
        @Override
        public boolean matches(CtLiteral literal) {
            return (literal.getParent() instanceof CtInvocation &&
                    !AmplificationChecker.isAssert((CtInvocation) literal.getParent())) ||
                    literal.getParent(CtAnnotation.class) == null
                            && super.matches(literal);
        }
    };

    @Override
    public List<CtMethod> apply(CtMethod testMethod) {
        List<CtLiteral> literals = testMethod.getElements(literalTypeFilter);
        return literals.stream()
                .filter(literal -> literal.getValue() instanceof Number)
                .flatMap(literal ->
                        this.amplify(literal).stream().map(newValue -> {
                            CtMethod clone = AmplificationHelper.cloneMethodTest(testMethod, "litNum");
                            clone.getElements(literalTypeFilter).get(literals.indexOf(literal)).replace(newValue);
                            return clone;
                        })
                ).collect(Collectors.toList());
    }

    private Set<CtLiteral<Number>> amplify(CtLiteral literal) {
        Set<CtLiteral<Number>> values = new HashSet<>();
        Double value = ((Number) literal.getValue()).doubleValue();

        //TODO asking myself if such transformation are useful...
//        values.add(literal.getFactory().createLiteral(value / 2));
//        values.add(literal.getFactory().createLiteral(value * 2));

        if (literal.getValue().getClass() == Byte.class || literal.getValue().getClass() == byte.class) {
            values.add(literal.getFactory().createLiteral(value.byteValue() + 1));
            values.add(literal.getFactory().createLiteral(value.byteValue() - 1));
            values.add(literal.getFactory().createLiteral(Byte.MAX_VALUE));
            values.add(literal.getFactory().createLiteral(Byte.MIN_VALUE));
            values.add(literal.getFactory().createLiteral((byte)0));
        }
        if (literal.getValue().getClass() == Short.class || literal.getValue().getClass() == short.class) {
            values.add(literal.getFactory().createLiteral(value.shortValue() + 1));
            values.add(literal.getFactory().createLiteral(value.shortValue() - 1));
            values.add(literal.getFactory().createLiteral(Short.MAX_VALUE));
            values.add(literal.getFactory().createLiteral(Short.MIN_VALUE));
            values.add(literal.getFactory().createLiteral((short)0));
        }
        if (literal.getValue().getClass() == Integer.class || literal.getValue().getClass() == int.class) {
            values.add(literal.getFactory().createLiteral(value.intValue() + 1));
            values.add(literal.getFactory().createLiteral(value.intValue() - 1));
            values.add(literal.getFactory().createLiteral(Integer.MAX_VALUE));
            values.add(literal.getFactory().createLiteral(Integer.MIN_VALUE));
            values.add(literal.getFactory().createLiteral(0));
        }
        if (literal.getValue().getClass() == Long.class || literal.getValue().getClass() == long.class) {
            values.add(literal.getFactory().createLiteral(value.longValue() + 1));
            values.add(literal.getFactory().createLiteral(value.longValue() - 1));
            values.add(literal.getFactory().createLiteral(Long.MAX_VALUE));
            values.add(literal.getFactory().createLiteral(Long.MIN_VALUE));
            values.add(literal.getFactory().createLiteral(0L));
        }
        if (literal.getValue().getClass() == Float.class || literal.getValue().getClass() == float.class) {
            values.add(literal.getFactory().createLiteral(value.floatValue() + 1));
            values.add(literal.getFactory().createLiteral(value.floatValue() - 1));
            values.add(literal.getFactory().createLiteral(Float.NaN));
            values.add(literal.getFactory().createLiteral(Float.POSITIVE_INFINITY));
            values.add(literal.getFactory().createLiteral(Float.NEGATIVE_INFINITY));
            values.add(literal.getFactory().createLiteral(Float.MIN_NORMAL));
            values.add(literal.getFactory().createLiteral(Float.MAX_VALUE));
            values.add(literal.getFactory().createLiteral(Float.MIN_VALUE));
            values.add(literal.getFactory().createLiteral(0.0F));
        }
        if (literal.getValue().getClass() == Double.class || literal.getValue().getClass() == double.class) {
            values.add(literal.getFactory().createLiteral(value + 1));
            values.add(literal.getFactory().createLiteral(value - 1));
            values.add(literal.getFactory().createLiteral(Double.NaN));
            values.add(literal.getFactory().createLiteral(Double.POSITIVE_INFINITY));
            values.add(literal.getFactory().createLiteral(Double.NEGATIVE_INFINITY));
            values.add(literal.getFactory().createLiteral(Double.MIN_NORMAL));
            values.add(literal.getFactory().createLiteral(Double.MAX_VALUE));
            values.add(literal.getFactory().createLiteral(Double.MIN_VALUE));
            values.add(literal.getFactory().createLiteral(0.0D));
        }
        return values;
    }

    @Override
    public CtMethod applyRandom(CtMethod testMethod) {
        return null;
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
    }
}
