package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import eu.stamp_project.utils.RandomHelper;
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
    public void testIntMutation() throws Exception {

        /*
            Test the amplification on numbers (integer) literal
                4 operations: i+1, i−1, i×2, i÷2.
                and 1 literals present that is different that the muted
                and 0
         */

        final String nameMethod = "methodInteger";
        final int originalValue = 23;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        TestDataMutator amplifier = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Integer> expectedValues = Arrays.asList(22, 24, 46, (23 / 2), 32, 0);

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());

        assertEquals(6, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + "Number" + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(originalValue, mutantLiteral.getValue());
            assertTrue(expectedValues.contains(mutantLiteral.getValue()));
        }
    }

    @Test
    public void testDoubleMutation() throws Exception {

        /*
            Test the amplification on numbers (double) literal
                4 operations: i+1, i−1, i×2, i÷2
                and 1 literals present that is different that the muted
         */
        final String nameMethod = "methodDouble";
        final double originalValue = 23.0D;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        TestDataMutator amplifier = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<Double> expectedValues = Arrays.asList(22.0D, 24.0D, 46.0D, (23.0D / 2.0D), 32.0D, 0.0D);

        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());

        assertEquals(6, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + "Number" + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(originalValue, mutantLiteral.getValue());
            assertTrue(expectedValues.contains(mutantLiteral.getValue()));
        }
    }

    @Test
    public void testStringMutation() throws Exception {

        /*
          Test the amplification on string literal
                3 operations: remove 1 random char, replace 1 random char, add 1 random char
                Additionally, it replace by totally random string with the same size than the original one,
                and by 1 literals present that is different that the muted
        */

        final String nameMethod = "methodString";
        final String originalValue = "MyStringLiteral";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        TestDataMutator amplifier = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());

        assertEquals(12, mutantMethods.size());
        for (int i = 0; i < 6; i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + "String" + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(originalValue, mutantLiteral.getValue());
            assertDistanceBetweenOriginalAndMuted(originalValue, (String) mutantLiteral.getValue());
        }
    }

    /**
     * method to compute the distance between an original string and a mutant string
     * this distance is equals to 1, since we do not stack mutation.
     * 3 cases: one char less, one char more and one (and only one) different char.
     */
    private void assertDistanceBetweenOriginalAndMuted(String original, String mutant) throws Exception {

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
                                return Utils.getFactory().Type().STRING.equals(literal.getType()) && super.matches(literal);
                            }
                        }).stream().map(ctLiteral -> (String) ctLiteral.getValue())
                ).collect(Collectors.toList());

        assertTrue(addCharAssertion ||
                removeCharAssertion ||
                (diffFound && replaceCharAssertion) ||
                existingStringLiterals.contains(mutant) ||
                "c!>ya,f,0(TDja_".equals(mutant));
    }

    @Test
    public void testBooleanMutation() throws Exception {

        /*
            Test the amplification on boolean literal
         */

        final String nameMethod = "methodBoolean";
        final boolean originalValue = true;
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        TestDataMutator mutator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = mutator.amplify(method, 0).collect(Collectors.toList());
        CtMethod mutantMethod = mutantMethods.get(0);

        assertEquals(1, mutantMethods.size());
        assertEquals(nameMethod + SUFFIX_MUTATION + "Boolean" + "1", mutantMethod.getSimpleName());
        CtLiteral mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertEquals(!(originalValue), mutantLiteral.getValue());
    }

    @Test
    public void testCharacterMutation() throws Exception {
        /*
            Test the amplification on character literal
         */

        final String nameMethod = "methodCharacter";
        final char originalValue = 'z';
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        TestDataMutator mutator = getTestDataMutator(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = mutator.amplify(method, 0).collect(Collectors.toList());
        List<Character> expectedValues = Arrays.asList('\0', ' ', 'h', '{', 'y', System.getProperty("line.separator").charAt(0));

        assertEquals(6, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + "Char" + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(originalValue, mutantLiteral.getValue());
            assertTrue(expectedValues.contains(mutantLiteral.getValue()));
        }
    }

    private TestDataMutator getTestDataMutator(CtClass<Object> literalMutationClass) {
        TestDataMutator amplifier = new TestDataMutator();
        amplifier.reset(literalMutationClass);
        return amplifier;
    }

}
