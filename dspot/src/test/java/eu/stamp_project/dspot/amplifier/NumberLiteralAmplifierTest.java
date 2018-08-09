package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Test;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NumberLiteralAmplifierTest extends AbstractTest {

    @Test
    public void testByteMutation() throws Exception {
        final String nameMethod = "methodByte";
        final byte originalValue = 23;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        NumberLiteralAmplifier amplifier = getAmplifier(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Byte> expectedValues = Arrays.asList((byte) 22, (byte) 24, (byte) 0, (byte) 53);
        List<String> expectedFieldReads = Arrays.asList(
                "java.lang.Byte.MAX_VALUE",
                "java.lang.Byte.MIN_VALUE"
        );

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(6, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + "litNum" + (i + 1), mutantMethod.getSimpleName());
            CtExpression mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtExpression.class)).get(0);
            if (mutantLiteral instanceof CtLiteral) {
                assertNotEquals(originalValue, ((CtLiteral<?>) mutantLiteral).getValue());
                assertTrue(((CtLiteral<?>) mutantLiteral).getValue() + " not in expected values",
                        expectedValues.contains(((CtLiteral<?>) mutantLiteral).getValue()));
            } else {
                assertTrue(mutantLiteral instanceof CtFieldRead);
                assertTrue(expectedFieldReads.contains(mutantLiteral.toString()));
            }
        }
    }

    @Test
    public void testShortMutation() throws Exception {
        final String nameMethod = "methodShort";
        final short originalValue = 23;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        NumberLiteralAmplifier amplifier = getAmplifier(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Short> expectedValues = Arrays.asList((short) 22, (short) 24, (short) 0, (short) -25291);
        List<String> expectedFieldReads = Arrays.asList(
                "java.lang.Short.MAX_VALUE",
                "java.lang.Short.MIN_VALUE"
        );

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(6, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + "litNum" + (i + 1), mutantMethod.getSimpleName());
            CtExpression mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtExpression.class)).get(0);
            if (mutantLiteral instanceof CtLiteral) {
                assertNotEquals(originalValue, ((CtLiteral<?>) mutantLiteral).getValue());
                assertTrue(((CtLiteral<?>) mutantLiteral).getValue() + " not in expected values",
                        expectedValues.contains(((CtLiteral<?>) mutantLiteral).getValue()));
            } else {
                assertTrue(mutantLiteral instanceof CtFieldRead);
                assertTrue(expectedFieldReads.contains(mutantLiteral.toString()));
            }
        }
    }

    @Test
    public void testIntMutation() throws Exception {
        final String nameMethod = "methodInteger";
        final int originalValue = 23;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        NumberLiteralAmplifier amplifier = getAmplifier(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Integer> expectedValues = Arrays.asList(22, 24, 2147483647, -2147483648, 0, -1170105035);
        List<String> expectedFieldReads = Arrays.asList(
                "java.lang.Integer.MAX_VALUE",
                "java.lang.Integer.MIN_VALUE"
        );

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(6, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + "litNum" + (i + 1), mutantMethod.getSimpleName());
            CtExpression mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtExpression.class)).get(0);
            if (mutantLiteral instanceof CtLiteral) {
                assertNotEquals(originalValue, ((CtLiteral<?>) mutantLiteral).getValue());
                assertTrue(((CtLiteral<?>) mutantLiteral).getValue() + " not in expected values",
                        expectedValues.contains(((CtLiteral<?>) mutantLiteral).getValue()));
            } else {
                assertTrue(mutantLiteral instanceof CtFieldRead);
                assertTrue(expectedFieldReads.contains(mutantLiteral.toString()));
            }
        }
    }

    @Test
    public void testLongMutation() throws Exception {
        final String nameMethod = "methodLong";
        final long originalValue = 23L;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        NumberLiteralAmplifier amplifier = getAmplifier(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Long> expectedValues = Arrays.asList(22L, 24L, 0L, -935319508L);
        List<String> expectedFieldReads = Arrays.asList(
                "java.lang.Long.MAX_VALUE",
                "java.lang.Long.MIN_VALUE"
        );

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(6, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + "litNum" + (i + 1), mutantMethod.getSimpleName());
            CtExpression mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtExpression.class)).get(0);
            if (mutantLiteral instanceof CtLiteral) {
                assertNotEquals(originalValue, ((CtLiteral<?>) mutantLiteral).getValue());
                assertTrue(((CtLiteral<?>) mutantLiteral).getValue() + " not in expected values",
                        expectedValues.contains(((CtLiteral<?>) mutantLiteral).getValue()));
            } else {
                assertTrue(mutantLiteral instanceof CtFieldRead);
                assertTrue(expectedFieldReads.contains(mutantLiteral.toString()));
            }
        }
    }

    @Test
    public void testFloatMutation() throws Exception {
        final String nameMethod = "methodFloat";
        final double originalValue = 23.0F;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        NumberLiteralAmplifier amplifier = getAmplifier(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Float> expectedValues = Arrays.asList(22.0F, 24.0F, 0.0F, 0.7275637F);
        List<String> expectedFieldReads = Arrays.asList(
                "java.lang.Float.MAX_VALUE",
                "java.lang.Float.MIN_VALUE",
                "java.lang.Float.MIN_NORMAL",
                "java.lang.Float.NaN",
                "java.lang.Float.POSITIVE_INFINITY",
                "java.lang.Float.NEGATIVE_INFINITY"
        );

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(10, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + "litNum" + (i + 1), mutantMethod.getSimpleName());
            CtExpression mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtExpression.class)).get(0);
            if (mutantLiteral instanceof CtLiteral) {
                assertNotEquals(originalValue, ((CtLiteral<?>) mutantLiteral).getValue());
                assertTrue(((CtLiteral<?>) mutantLiteral).getValue() + " not in expected values",
                        expectedValues.contains(((CtLiteral<?>) mutantLiteral).getValue()));
            } else {
                assertTrue(mutantLiteral instanceof CtFieldRead);
                assertTrue(expectedFieldReads.contains(mutantLiteral.toString()));
            }
        }
    }


    @Test
    public void testDoubleMutation() throws Exception {
        final String nameMethod = "methodDouble";
        final double originalValue = 23.0D;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        NumberLiteralAmplifier amplifier = getAmplifier(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Double> expectedValues = Arrays.asList(22.0D, 24.0D, 0.0D, 0.7275636800328681);
        List<String> expectedFieldReads = Arrays.asList("java.lang.Double.MAX_VALUE",
                "java.lang.Double.MIN_VALUE", "java.lang.Double.MIN_NORMAL",
                "java.lang.Double.NaN", "java.lang.Double.POSITIVE_INFINITY",
                "java.lang.Double.NEGATIVE_INFINITY"
        );

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(10, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + "litNum" + (i + 1), mutantMethod.getSimpleName());
            CtExpression mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtExpression.class)).get(0);
            if (mutantLiteral instanceof CtLiteral) {
                assertNotEquals(originalValue, ((CtLiteral<?>) mutantLiteral).getValue());
                assertTrue(((CtLiteral<?>) mutantLiteral).getValue() + " not in expected values",
                        expectedValues.contains(((CtLiteral<?>) mutantLiteral).getValue()));
            } else {
                assertTrue(mutantLiteral instanceof CtFieldRead);
                assertTrue(expectedFieldReads.contains(mutantLiteral.toString()));
            }
        }
    }

    private NumberLiteralAmplifier getAmplifier(CtClass<Object> literalMutationClass) {
        NumberLiteralAmplifier amplifier = new NumberLiteralAmplifier();
        amplifier.reset(literalMutationClass);
        return amplifier;
    }

}
