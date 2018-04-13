package fr.inria.diversify.dspot.amplifier;

import spoon.reflect.code.CtLiteral;

import java.util.HashSet;
import java.util.Set;

public class NumberLiteralAmplifier extends AbstractLiteralAmplifier<Number> {

    @Override
    protected Set<Number> amplify(CtLiteral<Number> literal) {
        Set<Number> values = new HashSet<>();
        Double value = ((Number) literal.getValue()).doubleValue();

        Class<?> classOfLiteral;
        if (! literal.getTypeCasts().isEmpty()){
            classOfLiteral = literal.getTypeCasts().get(0).getActualClass();
        } else {
            classOfLiteral = literal.getType().getActualClass();
        }

        if (classOfLiteral == Byte.class || classOfLiteral == byte.class) {
            values.add((byte)(value.byteValue() + 1));
            values.add((byte)(value.byteValue() - 1));
            values.add(Byte.MAX_VALUE);
            values.add(Byte.MIN_VALUE);
            values.add((byte)0);
        }
        if (classOfLiteral == Short.class || classOfLiteral == short.class) {
            values.add((short)(value.shortValue() + 1));
            values.add((short)(value.shortValue() - 1));
            values.add(Short.MAX_VALUE);
            values.add(Short.MIN_VALUE);
            values.add((short)0);
        }
        if (classOfLiteral == Integer.class || classOfLiteral == int.class) {
            values.add(value.intValue() + 1);
            values.add(value.intValue() - 1);
            values.add(Integer.MAX_VALUE);
            values.add(Integer.MIN_VALUE);
            values.add(0);
        }
        if (classOfLiteral == Long.class || classOfLiteral == long.class) {
            values.add((long)(value.longValue() + 1));
            values.add((long)(value.longValue() - 1));
            values.add(Long.MAX_VALUE);
            values.add(Long.MIN_VALUE);
            values.add(0L);
        }
        if (classOfLiteral == Float.class || classOfLiteral == float.class) {
            values.add(value.floatValue() + 1.0F);
            values.add(value.floatValue() - 1.0F);
            values.add(Float.NaN);
            values.add(Float.POSITIVE_INFINITY);
            values.add(Float.NEGATIVE_INFINITY);
            values.add(Float.MIN_NORMAL);
            values.add(Float.MAX_VALUE);
            values.add(Float.MIN_VALUE);
            values.add(0.0F);
        }
        if (classOfLiteral == Double.class || classOfLiteral == double.class) {
            values.add(value + 1.0D);
            values.add(value - 1.0D);
            values.add(Double.NaN);
            values.add(Double.POSITIVE_INFINITY);
            values.add(Double.NEGATIVE_INFINITY);
            values.add(Double.MIN_NORMAL);
            values.add(Double.MAX_VALUE);
            values.add(Double.MIN_VALUE);
            values.add(0.0D);
        }
        return values;
    }

    @Override
    protected String getSuffix() {
        return "litNum";
    }

    @Override
    protected Class<?> getTargetedClass() {
        return Number.class;
    }
}
