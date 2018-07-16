package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public class NullifierAmplifierTest extends AbstractTest {

    @Test
    public void test() throws Exception {

        // test the result of the NullifierAmplifier

        final String nameMethod = "methodString";
        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        Amplifier amplifier = new NullifierAmplifier();
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);
        List<CtMethod<?>> amplification = amplifier.apply(method).collect(Collectors.toList());
        assertEquals(4, amplification.size());
        amplification = amplification.stream().flatMap(amplifier::apply).collect(Collectors.toList());
        assertEquals(12, amplification.size());
    }
}
