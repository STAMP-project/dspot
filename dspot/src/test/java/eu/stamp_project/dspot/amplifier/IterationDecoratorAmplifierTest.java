package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public class IterationDecoratorAmplifierTest extends AbstractTest {

    @Test
    public void test() throws Exception {

        /*
            Test the application of IterationDecoratorAmplifier.
            This amplifier decorate the given amplifier to apply it at a given frequency
         */

        final String nameMethod = "methodInteger";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        // we apply the given amplifier each 2 iteration, i.e. iteration 0, iteration 2, iteration 4 etc....
        Amplifier amplifier = new IterationDecoratorAmplifier(new NumberLiteralAmplifier(), 2);
        CtMethod method = literalMutationClass.getMethod(nameMethod);

        // 1 / 2 iteration produces something (the list is not empty)
        for (int i = 0 ; i < 10 ; i = i + 2) {
            assertFalse(amplifier.amplify(method, i).collect(Collectors.toList()).isEmpty());
            assertTrue(amplifier.amplify(method, i + 1).collect(Collectors.toList()).isEmpty());
        }

        amplifier = new IterationDecoratorAmplifier(new NumberLiteralAmplifier(), 3);
         // 1 / 3 iteration produces something (the list is not empty)
        for (int i = 0 ; i < 12 ; i = i + 3) {
            assertFalse(amplifier.amplify(method, i).collect(Collectors.toList()).isEmpty());
            assertTrue(amplifier.amplify(method, i + 1).collect(Collectors.toList()).isEmpty());
            assertTrue(amplifier.amplify(method, i + 2).collect(Collectors.toList()).isEmpty());
        }

    }
}
