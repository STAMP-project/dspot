package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.amplifier.amplifiers.Amplifier;
import eu.stamp_project.dspot.amplifier.amplifiers.CharLiteralAmplifier;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;

public class CharacterLiteralAmplifierTest extends AbstractTestOnSample {

    CharLiteralAmplifier amplifier;

    CtClass<?> literalMutationClass;

    @Before
    public void setup() throws Exception {
        super.setUp();
        literalMutationClass = findClass("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        amplifier = new CharLiteralAmplifier();
    }

    @Test
    public void testAmplify() {
        final String nameMethod = "methodCharacter";
        amplifier.reset(literalMutationClass);
        CtClass<Object> literalMutationClass = launcher.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        Amplifier mutator = new CharLiteralAmplifier();
        mutator.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(6, mutantMethods.size());
    }

    @Test
    public void testAmplifyNull() {
        final String nameMethod = "methodNullCharacter";
        amplifier.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(4, mutantMethods.size());
    }
}
