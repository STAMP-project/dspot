package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CharacterLiteralAmplifierTest extends AbstractTest {

    @Test
    public void testAmplify() throws Exception {
        final String nameMethod = "methodCharacter";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        Amplifier mutator = new CharLiteralAmplifier();
        mutator.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = mutator.amplify(method, 0).collect(Collectors.toList());
        assertEquals(6, mutantMethods.size());
    }

}
