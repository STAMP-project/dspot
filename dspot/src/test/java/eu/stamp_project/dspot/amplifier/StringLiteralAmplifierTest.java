package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringLiteralAmplifierTest extends AbstractTest {

    @Test
    public void testAmplify() throws Exception {

        /*
            test the StringLiteral
            The first iteration is complete, i.e. apply random operations plus the specific strings
            the second iteration does not apply the specific strings, we already did
            After the reset, the amplifier should obtain the same number than a the first iteration.
         */

        final String nameMethod = "methodString";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        Amplifier amplificator = new StringLiteralAmplifier();
        amplificator.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplificator.apply(method);
        assertEquals(43, mutantMethods.size());
        mutantMethods = amplificator.apply(method);
        assertEquals(23, mutantMethods.size());
        amplificator.reset(literalMutationClass);
        mutantMethods = amplificator.apply(method);
        assertEquals(43, mutantMethods.size());
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
