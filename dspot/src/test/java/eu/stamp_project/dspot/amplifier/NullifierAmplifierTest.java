package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public class NullifierAmplifierTest extends AbstractTest {

    @Test
    public void testOnArrayType() throws Exception {
        final String nameMethod = "methodThatClassmethodWithCharArray";
        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        Amplifier amplifier = new NullifierAmplifier();
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);
        List<CtMethod<?>> amplification = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertTrue(amplification.isEmpty());
    }

    @Test
    public void test() throws Exception {

        // test the result of the NullifierAmplifier

        final String nameMethod = "methodString";
        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        Amplifier amplifier = new NullifierAmplifier();
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);
        List<CtMethod<?>> amplification = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(2, amplification.size());
        amplification = amplification.stream().flatMap(testMethod -> amplifier.amplify(testMethod, 0)).collect(Collectors.toList());
        assertEquals(2, amplification.size());
    }

    @Test
    public void testOnInteger() throws Exception {

        // test the result of the NullifierAmplifier

        final String nameMethod = "methodInteger";
        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        Amplifier amplifier = new NullifierAmplifier();
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);
        List<CtMethod<?>> amplification = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(0, amplification.size());
    }

    @Test
    public void testOnIntegerMethodCall() throws Exception {

        /*
            test the result of the NullifierAmplifier: it can nullify non primitive type but not primitive
         */

        final String nameMethod = "methodCall";
        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.ClassWithMethodCall");
        RandomHelper.setSeedRandom(42L);
        Amplifier amplifier = new NullifierAmplifier();
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);
        List<CtMethod<?>> amplification = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(2, amplification.size());
        amplification = amplification.stream().flatMap(testMethod -> amplifier.amplify(testMethod, 0)).collect(Collectors.toList());
        assertEquals(2, amplification.size());
    }
}
