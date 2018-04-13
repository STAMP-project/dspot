package fr.inria.diversify.dspot.amplifier;

import fr.inria.Utils;
import fr.inria.AbstractTest;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringLiteralAmplifierTest extends AbstractTest {

    @Test
    public void testAmplify() throws Exception {
        final String nameMethod = "methodString";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        Amplifier amplificator = new StringLiteralAmplifier();
        amplificator.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplificator.apply(method);
        assertEquals(46, mutantMethods.size());
    }

    @Test
    public void testDoesNotAmplifyChar() throws Exception {
        final String nameMethod = "methodCharacter";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        Amplifier mutator = new StringLiteralAmplifier();
        mutator.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = mutator.apply(method);
        assertTrue(mutantMethods.isEmpty());
    }
}
