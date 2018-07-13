package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/18
 */
public class LiteralAmplifiersTest extends AbstractTest {

    @Test
    public void test() throws Exception {

        /*
            This test the application of multiple amplifier, multiple time
            The multiple applications of amplifiers should result with non-redundant amplified test
            Here, we test that amplifiers marks amplified nodes and do not amplify them again
            This avoid redundant transformation,
            and thus improve the global performance in term of memory and execution time of DSpot
         */

        final String nameMethod = "methodString";
        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        AmplificationHelper.setSeedRandom(42L);
        Amplifier stringLiteralAmplifier = new StringLiteralAmplifier();
        stringLiteralAmplifier.reset(literalMutationClass);
        Amplifier numberLiteralAmplifier= new NumberLiteralAmplifier();
        numberLiteralAmplifier.reset(literalMutationClass);
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);

        // 1rst application of both amplifiers
        Stream<CtMethod<?>> amplifiedStringMethods = stringLiteralAmplifier.apply(method);
        Stream<CtMethod<?>> amplifiedNumberMethods = numberLiteralAmplifier.apply(method);

        final List<CtMethod<?>> amplifiedMethods = Stream.concat(amplifiedNumberMethods, amplifiedStringMethods).collect(Collectors.toList());
        assertEquals(28, amplifiedMethods.size());

        // 2nd application of both amplifiers:
        amplifiedStringMethods = amplifiedMethods.stream().flatMap(stringLiteralAmplifier::apply);
        amplifiedNumberMethods = amplifiedMethods.stream().flatMap(numberLiteralAmplifier::apply);
        //here, we have less amplified test method than before from more than 1k to 366
        assertEquals(366, Stream.concat(amplifiedStringMethods, amplifiedNumberMethods).count());
    }
}
