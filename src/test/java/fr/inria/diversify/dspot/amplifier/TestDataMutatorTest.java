package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.dspot.AmplificationHelper;
import org.junit.Test;
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

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/23/16
 */
public class TestDataMutatorTest extends AbstractTest {

    private static final String SUFFIX_MUTATION = "_literalMutation";

    @Test
    public void testIntMutation() throws Exception, InvalidSdkException {

        /*
            Test the amplification on numbers (integer) literal
                4 operations: i+1, i−1, i×2, i÷2.
                and 1 literals present that is different that the muted
         */

        final String nameMethod = "methodInteger";
        final int originalValue = 23;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        TestDataMutator amplificator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Integer> expectedValues = Arrays.asList(22, 24, 46, (23 / 2), 32);

        List<CtMethod> mutantMethods = amplificator.apply(method);

        assertEquals(5, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(originalValue, mutantLiteral.getValue());
            assertTrue(expectedValues.contains(mutantLiteral.getValue()));
        }
    }

    @Test
    public void testIntMutationRandom() throws Exception, InvalidSdkException {

        /*
            Test the amplification on numbers (integer) literal
                4 operations: i+1, i−1, i×2, i÷2.
                and 1 literals present that is different that the muted
                One random amplification at the time
         */

        final String nameMethod = "methodInteger";
        final int originalValue = 23;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        TestDataMutator amplificator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Integer> expectedValues = Arrays.asList(22, 24, 46, (23 / 2), 32);

        CtMethod mutantMethod = amplificator.applyRandom(method);

        assertEquals(nameMethod + SUFFIX_MUTATION + 1, mutantMethod.getSimpleName());
        CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertNotEquals(originalValue, mutantLiteral.getValue());
        assertTrue(expectedValues.contains(mutantLiteral.getValue()));

        CtMethod mutantMethod2 = amplificator.applyRandom(method);

        assertEquals(nameMethod + SUFFIX_MUTATION + 2, mutantMethod2.getSimpleName());
        CtLiteral mutantLiteral2 = mutantMethod2.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertNotEquals(originalValue, mutantLiteral2.getValue());
        assertTrue(expectedValues.contains(mutantLiteral2.getValue()));

        assertNotEquals(mutantLiteral.getValue(), mutantLiteral2.getValue());
    }


    @Test
    public void testDoubleMutation() throws Exception, InvalidSdkException {

        /*
            Test the amplification on numbers (double) literal
                4 operations: i+1, i−1, i×2, i÷2
                and 1 literals present that is different that the muted
         */
        final String nameMethod = "methodDouble";
        final double originalValue = 23.0D;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        TestDataMutator amplificator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Double> expectedValues = Arrays.asList(22.0D, 24.0D, 46.0D, (23.0D / 2.0D), 32.0D);

        List<CtMethod> mutantMethods = amplificator.apply(method);

        assertEquals(5, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(originalValue, mutantLiteral.getValue());
            assertTrue(expectedValues.contains(mutantLiteral.getValue()));
        }
    }

    @Test
    public void testDoubleMutationRandom() throws Exception, InvalidSdkException {

        /*
            Test the amplification on numbers (double) literal
                4 operations: i+1, i−1, i×2, i÷2
                and 1 literals present that is different that the muted
                One random amplification at the time
         */
        final String nameMethod = "methodDouble";
        final double originalValue = 23.0D;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        TestDataMutator amplificator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Double> expectedValues = Arrays.asList(22.0D, 24.0D, 46.0D, (23.0D / 2.0D), 32.0D);

        CtMethod mutantMethod = amplificator.applyRandom(method);

        assertEquals(nameMethod + SUFFIX_MUTATION + 1, mutantMethod.getSimpleName());
        CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertNotEquals(originalValue, mutantLiteral.getValue());
        assertTrue(expectedValues.contains(mutantLiteral.getValue()));

        CtMethod mutantMethod2 = amplificator.applyRandom(method);

        assertEquals(nameMethod + SUFFIX_MUTATION + 2, mutantMethod2.getSimpleName());
        CtLiteral mutantLiteral2 = mutantMethod2.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertNotEquals(originalValue, mutantLiteral2.getValue());
        assertTrue(expectedValues.contains(mutantLiteral2.getValue()));

        assertNotEquals(mutantLiteral.getValue(), mutantLiteral2.getValue());
    }


    @Test
    public void testStringMutation() throws Exception, InvalidSdkException {

        /*
          Test the amplification on string literal
                3 operations: remove 1 random char, replace 1 random char, add 1 random char
                and 1 literals present that is different that the muted
        */

        final String nameMethod = "methodString";
        final String originalValue = "MyStringLiteral";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        TestDataMutator amplificator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplificator.apply(method);

        assertEquals(4, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(originalValue, mutantLiteral.getValue());
            assertDistanceBetweenOriginalAndMuted(originalValue, (String) mutantLiteral.getValue());
        }
    }

    @Test
    public void testStringMutationRandom() throws Exception, InvalidSdkException {

        /*
          Test the amplification on string literal
                3 operations: remove 1 random char, replace 1 random char, add 1 random char
                and 1 literals present that is different that the muted
                One random amplification at the time
        */

        final String nameMethod = "methodString";
        final String originalValue = "MyStringLiteral";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        TestDataMutator amplificator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);

        CtMethod mutantMethod = amplificator.applyRandom(method);

        assertEquals(nameMethod + SUFFIX_MUTATION + 1, mutantMethod.getSimpleName());
        CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertNotEquals(originalValue, mutantLiteral.getValue());
        assertDistanceBetweenOriginalAndMuted(originalValue, (String) mutantLiteral.getValue());

        CtMethod mutantMethod2 = amplificator.applyRandom(method);

        assertEquals(nameMethod + SUFFIX_MUTATION + 2, mutantMethod2.getSimpleName());
        CtLiteral mutantLiteral2 = mutantMethod2.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertNotEquals(originalValue, mutantLiteral2.getValue());
        assertDistanceBetweenOriginalAndMuted(originalValue, (String) mutantLiteral.getValue());

        assertNotEquals(mutantLiteral.getValue(), mutantLiteral2.getValue());
    }

    /**
     * method to compute the distance between an original string and a mutant string
     * this distance is equals to 1, since we do not stack mutation.
     * 3 cases: one char less, one char more and one (and only one) different char.
     */
    private void assertDistanceBetweenOriginalAndMuted(String original, String mutant) throws InvalidSdkException, Exception {

        byte[] originalBytes = original.getBytes();
        byte[] mutantBytes = mutant.getBytes();

        boolean addCharAssertion = originalBytes.length == mutantBytes.length + 1;
        boolean removeCharAssertion = originalBytes.length == mutantBytes.length - 1;

        boolean replaceCharAssertion = false;
        boolean diffFound = false;
        if (originalBytes.length == mutantBytes.length) {
            diffFound = false;
            for (int i = 0; i < originalBytes.length; i++) {
                if (originalBytes[i] != mutantBytes[i] && diffFound) {
                    replaceCharAssertion = false;
                    break;
                } else if (originalBytes[i] != mutantBytes[i]) {
                    diffFound = true;
                    replaceCharAssertion = true;
                }
            }
        }

        List<String> existingStringLiterals = Utils.getFactory().Class().getAll()
                .stream()
                .flatMap(ctType ->
                        ctType.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
                            @Override
                            public boolean matches(CtLiteral literal) {
                                try {
                                    return Utils.getFactory().Type().STRING.equals(literal.getType()) && super.matches(literal);
                                } catch (InvalidSdkException | Exception ignored) {
                                }
                                return false;
                            }
                        }).stream().map(ctLiteral -> (String) ctLiteral.getValue())
                ).collect(Collectors.toList());

        assertTrue(addCharAssertion || removeCharAssertion || (diffFound && replaceCharAssertion) || existingStringLiterals.contains(mutant));
    }

    @Test
    public void testBooleanMutation() throws Exception, InvalidSdkException {

        /*
            Test the amplification on boolean literal
         */

        final String nameMethod = "methodBoolean";
        final boolean originalValue = true;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        TestDataMutator mutator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = mutator.apply(method);
        CtMethod mutantMethod = mutantMethods.get(0);

        assertEquals(1, mutantMethods.size());
        assertEquals(nameMethod + SUFFIX_MUTATION + "1", mutantMethod.getSimpleName());
        CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertEquals(!(originalValue), mutantLiteral.getValue());
    }

    private TestDataMutator getTestDataMutator(CtClass<Object> literalMutationClass) {
        TestDataMutator amplificator = new TestDataMutator();
        amplificator.reset(literalMutationClass);
        return amplificator;
    }

}
