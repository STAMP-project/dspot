package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.RandomHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;

import java.util.HashSet;
import java.util.Set;

public class NumberLiteralAmplifier extends AbstractLiteralAmplifier<Number> {

    private static CtFieldRead<Number> getCtFieldRead(Class<?> clazz, String fieldName, Factory factory) {
        final CtFieldRead<Number> fieldRead = factory.createFieldRead();
        final CtClass<?> doubleClass = factory.Class().get(clazz);
        final CtField<Number> field = (CtField<Number>) doubleClass.getField(fieldName);
        final CtFieldReference<Number> reference = field.getReference();
        fieldRead.setVariable(reference);
        return fieldRead;
    }

    @Override
    protected Set<CtExpression<Number>> amplify(CtExpression<Number> literal, CtMethod<?> testMethod) {
        final Factory factory = testMethod.getFactory();
        Set<CtExpression<Number>> values = new HashSet<>();
        Double value = ((Number) ((CtLiteral<Number>)literal).getValue()).doubleValue();

        Class<?> classOfLiteral;
        if (! literal.getTypeCasts().isEmpty()){
            classOfLiteral = literal.getTypeCasts().get(0).getActualClass();
        } else {
            classOfLiteral = literal.getType().getActualClass();
        }

        if (classOfLiteral == Byte.class || classOfLiteral == byte.class) {
            values.add(factory.createLiteral((byte)(value.byteValue() + 1)));
            values.add(factory.createLiteral((byte)(value.byteValue() - 1)));
            values.add(getCtFieldRead(Byte.class, "MAX_VALUE", factory));
            values.add(getCtFieldRead(Byte.class, "MIN_VALUE", factory));
            values.add(factory.createLiteral((byte)0));
            values.add(factory.createLiteral((byte) RandomHelper.getRandom().nextInt()));
        }
        if (classOfLiteral == Short.class || classOfLiteral == short.class) {
            values.add(factory.createLiteral((short)(value.shortValue() + 1)));
            values.add(factory.createLiteral((short)(value.shortValue() - 1)));
            values.add(getCtFieldRead(Short.class, "MAX_VALUE", factory));
            values.add(getCtFieldRead(Short.class, "MIN_VALUE", factory));
            values.add(factory.createLiteral((short)0));
            values.add(factory.createLiteral((short) RandomHelper.getRandom().nextInt()));
        }
        if (classOfLiteral == Integer.class || classOfLiteral == int.class) {
            values.add(factory.createLiteral(value.intValue() + 1));
            values.add(factory.createLiteral(value.intValue() - 1));
            values.add(getCtFieldRead(Integer.class, "MAX_VALUE", factory));
            values.add(getCtFieldRead(Integer.class, "MIN_VALUE", factory));
            values.add(factory.createLiteral(0));
            values.add(factory.createLiteral(RandomHelper.getRandom().nextInt()));
        }
        if (classOfLiteral == Long.class || classOfLiteral == long.class) {
            values.add(factory.createLiteral((long)(value.longValue() + 1)));
            values.add(factory.createLiteral((long)(value.longValue() - 1)));
            values.add(getCtFieldRead(Long.class, "MAX_VALUE", factory));
            values.add(getCtFieldRead(Long.class, "MIN_VALUE", factory));
            values.add(factory.createLiteral(0L));
            long randomLong = (long) RandomHelper.getRandom().nextInt();
            randomLong += (long) RandomHelper.getRandom().nextInt();
            values.add(factory.createLiteral(randomLong));
        }
        if (classOfLiteral == Float.class || classOfLiteral == float.class) {
            values.add(factory.createLiteral(value.floatValue() + 1.0F));
            values.add(factory.createLiteral(value.floatValue() - 1.0F));
            values.add(getCtFieldRead(Float.class, "NaN", factory));
            values.add(getCtFieldRead(Float.class, "POSITIVE_INFINITY", factory));
            values.add(getCtFieldRead(Float.class, "NEGATIVE_INFINITY", factory));
            values.add(getCtFieldRead(Float.class, "MIN_NORMAL", factory));
            values.add(getCtFieldRead(Float.class, "MAX_VALUE", factory));
            values.add(getCtFieldRead(Float.class, "MIN_VALUE", factory));
            values.add(factory.createLiteral(0.0F));
            values.add(factory.createLiteral((float) RandomHelper.getRandom().nextDouble()));
        }
        if (classOfLiteral == Double.class || classOfLiteral == double.class) {
            values.add(factory.createLiteral(value + 1.0D));
            values.add(factory.createLiteral(value - 1.0D));
            values.add(getCtFieldRead(Double.class, "NaN", factory));
            values.add(getCtFieldRead(Double.class, "POSITIVE_INFINITY", factory));
            values.add(getCtFieldRead(Double.class, "NEGATIVE_INFINITY", factory));
            values.add(getCtFieldRead(Double.class, "MIN_NORMAL", factory));
            values.add(getCtFieldRead(Double.class, "MAX_VALUE", factory));
            values.add(getCtFieldRead(Double.class, "MIN_VALUE", factory));
            values.add(factory.createLiteral(0.0D));
            values.add(factory.createLiteral(RandomHelper.getRandom().nextDouble()));
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
