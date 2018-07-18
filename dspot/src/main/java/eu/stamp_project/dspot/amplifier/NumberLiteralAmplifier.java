package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationHelper;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.HashSet;
import java.util.Set;

public class NumberLiteralAmplifier extends AbstractLiteralAmplifier<Number> {

    @Override
    protected Set<CtLiteral<Number>> amplify(CtLiteral<Number> literal, CtMethod<?> testMethod) {
        final Factory factory = testMethod.getFactory();
        Set<CtLiteral<Number>> values = new HashSet<>();
        Double value = ((Number) literal.getValue()).doubleValue();

        Class<?> classOfLiteral;
        if (! literal.getTypeCasts().isEmpty()){
            classOfLiteral = literal.getTypeCasts().get(0).getActualClass();
        } else {
            classOfLiteral = literal.getType().getActualClass();
        }

        if (classOfLiteral == Byte.class || classOfLiteral == byte.class) {
            values.add(factory.createLiteral((byte)(value.byteValue() + 1)));
            values.add(factory.createLiteral((byte)(value.byteValue() - 1)));
            values.add(factory.createLiteral(Byte.MAX_VALUE));
            values.add(factory.createLiteral(Byte.MIN_VALUE));
            values.add(factory.createLiteral((byte)0));
            values.add(factory.createLiteral((byte)AmplificationHelper.getRandom().nextInt()));
        }
        if (classOfLiteral == Short.class || classOfLiteral == short.class) {
            values.add(factory.createLiteral((short)(value.shortValue() + 1)));
            values.add(factory.createLiteral((short)(value.shortValue() - 1)));
            values.add(factory.createLiteral(Short.MAX_VALUE));
            values.add(factory.createLiteral(Short.MIN_VALUE));
            values.add(factory.createLiteral((short)0));
            values.add(factory.createLiteral((short)AmplificationHelper.getRandom().nextInt()));
        }
        if (classOfLiteral == Integer.class || classOfLiteral == int.class) {
            values.add(factory.createLiteral(value.intValue() + 1));
            values.add(factory.createLiteral(value.intValue() - 1));
            values.add(factory.createLiteral(Integer.MAX_VALUE));
            values.add(factory.createLiteral(Integer.MIN_VALUE));
            values.add(factory.createLiteral(0));
            values.add(factory.createLiteral(AmplificationHelper.getRandom().nextInt()));
        }
        if (classOfLiteral == Long.class || classOfLiteral == long.class) {
            values.add(factory.createLiteral((long)(value.longValue() + 1)));
            values.add(factory.createLiteral((long)(value.longValue() - 1)));
            values.add(factory.createLiteral(Long.MAX_VALUE));
            values.add(factory.createLiteral(Long.MIN_VALUE));
            values.add(factory.createLiteral(0L));
            long randomLong = (long)AmplificationHelper.getRandom().nextInt();
            randomLong += (long)AmplificationHelper.getRandom().nextInt();
            values.add(factory.createLiteral(randomLong));
        }
        if (classOfLiteral == Float.class || classOfLiteral == float.class) {
            values.add(factory.createLiteral(value.floatValue() + 1.0F));
            values.add(factory.createLiteral(value.floatValue() - 1.0F));
            values.add(factory.createLiteral(Float.NaN));
            values.add(factory.createLiteral(Float.POSITIVE_INFINITY));
            values.add(factory.createLiteral(Float.NEGATIVE_INFINITY));
            values.add(factory.createLiteral(Float.MIN_NORMAL));
            values.add(factory.createLiteral(Float.MAX_VALUE));
            values.add(factory.createLiteral(Float.MIN_VALUE));
            values.add(factory.createLiteral(0.0F));
            values.add(factory.createLiteral((float)AmplificationHelper.getRandom().nextDouble()));
        }
        if (classOfLiteral == Double.class || classOfLiteral == double.class) {
            values.add(factory.createLiteral(value + 1.0D));
            values.add(factory.createLiteral(value - 1.0D));
            values.add(factory.createLiteral(Double.NaN));
            values.add(factory.createLiteral(Double.POSITIVE_INFINITY));
            values.add(factory.createLiteral(Double.NEGATIVE_INFINITY));
            values.add(factory.createLiteral(Double.MIN_NORMAL));
            values.add(factory.createLiteral(Double.MAX_VALUE));
            values.add(factory.createLiteral(Double.MIN_VALUE));
            values.add(factory.createLiteral(0.0D));
            values.add(factory.createLiteral(AmplificationHelper.getRandom().nextDouble()));
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
