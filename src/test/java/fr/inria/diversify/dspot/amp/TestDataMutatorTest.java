package fr.inria.diversify.dspot.amp;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Benjamin DANGLOT (benjamin.danglot@inria.fr) on 11/23/16.
 */
public class TestDataMutatorTest {

    private static final String SUFFIX_MUTATION = "_literalMutation";

    @Test
    public void testIntMutation() throws Exception {
        final String nameMethod = "methodInt";
        CtClass<Object> literalMutationClass = buildLiteralMutationCtClass();
        TestDataMutator mutator = new TestDataMutator();
        mutator.reset(null, null, literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        CtLiteral literalOriginal = method.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        List<CtMethod> mutantMethods = mutator.apply(method);

        assertEquals(4, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(literalOriginal.getValue(), mutantLiteral.getValue());
        }
    }

    @Test
    public void testStringMutation() throws Exception {
        final String nameMethod = "methodString";
        CtClass<Object> literalMutationClass = buildLiteralMutationCtClass();
        TestDataMutator mutator = new TestDataMutator();
        mutator.reset(null, null, literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        CtLiteral literalOriginal = method.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        List<CtMethod> mutantMethods = mutator.apply(method);

        assertEquals(3, mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(nameMethod + SUFFIX_MUTATION + (i + 1), mutantMethod.getSimpleName());
            CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
            assertNotEquals(literalOriginal.getValue(), mutantLiteral.getValue());
        }
    }

    @Test
    public void testBooleanMutation() throws Exception {
        final String nameMethod = "methodBoolean";
        CtClass<Object> literalMutationClass = buildLiteralMutationCtClass();
        TestDataMutator mutator = new TestDataMutator();
        mutator.reset(null, null, literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        CtLiteral literalOriginal = method.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        List<CtMethod> mutantMethods = mutator.apply(method);
        CtMethod mutantMethod = mutantMethods.get(0);

        assertEquals(1, mutantMethods.size());
        assertEquals(nameMethod + SUFFIX_MUTATION + "1", mutantMethod.getSimpleName());
        CtLiteral mutantLiteral = mutantMethod.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        assertNotEquals(literalOriginal.getValue(), mutantLiteral.getValue());
    }

    private CtClass<Object> buildLiteralMutationCtClass() {
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/resources/mutation/LiteralMutation.java");
        launcher.buildModel();
        return launcher.getFactory().Class().get("mutation.LiteralMutation");
    }
}
