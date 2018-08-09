package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/18
 */
public class LiteralAmplifiersTest extends AbstractTest {

    @Ignore
    @Test
    public void testAllHasBeenAmplified() throws Exception {

        /*
            The amplifiers must keep doing the amplification in case of all the combination of amplification has been explored.
         */

        final CtClass testClass = Utils.findClass("fr.inria.workload.WorkloadTest");

        List<CtMethod<?>> allTest = AmplificationHelper.getAllTest(testClass);
        Amplifier amplifier = new NumberLiteralAmplifier();
        allTest = allTest.stream()
                .flatMap(method -> amplifier.amplify(method, 0)) // we apply twice the NumberLiteralAmplifier. In one iteration, it explores every amplification that can be done
                .flatMap(method -> amplifier.amplify(method, 0)) // however, we can continue to amplify since there is some random
                .collect(Collectors.toList());

        assertFalse(allTest.isEmpty());
    }

    @Test
    public void test() throws Exception {

        /*
            This test the application of multiple amplifier, multiple time
            The multiple applications of amplifiers should result with non-redundant amplified test
            Here, we test that amplifiers marks amplified nodes and do not amplify them again
            This avoid redundant transformation,
            and thus improve the global performance in term of memory and execution time of DSpot
         */

        final String nameMethod = "testInt";
        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.ampl.ToBeAmplifiedLiteralTest");
        RandomHelper.setSeedRandom(42L);
        Amplifier stringLiteralAmplifier = new StringLiteralAmplifier();
        stringLiteralAmplifier.reset(literalMutationClass);
        Amplifier numberLiteralAmplifier= new NumberLiteralAmplifier();
        numberLiteralAmplifier.reset(literalMutationClass);
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);

        // 1rst application of both amplifiers
        List<CtMethod<?>> amplifiedStringMethods = stringLiteralAmplifier.amplify(method, 0).collect(Collectors.toList());
        List<CtMethod<?>> amplifiedNumberMethods = numberLiteralAmplifier.amplify(method, 0).collect(Collectors.toList());

        List<CtMethod<?>> amplifiedMethods = new ArrayList<>();
        amplifiedMethods.addAll(amplifiedStringMethods);
        amplifiedMethods.addAll(amplifiedNumberMethods);
        assertEquals(47, amplifiedMethods.size());

        // 2nd application of both amplifiers:
        amplifiedStringMethods = amplifiedMethods.stream().flatMap(testMethod -> stringLiteralAmplifier.amplify(testMethod, 0)).collect(Collectors.toList());
        amplifiedNumberMethods = amplifiedMethods.stream().flatMap(testMethod -> numberLiteralAmplifier.amplify(testMethod, 0)).collect(Collectors.toList());

        amplifiedMethods.clear();
        amplifiedMethods.addAll(amplifiedStringMethods);
        amplifiedMethods.addAll(amplifiedNumberMethods);
        //here, we have less amplified test method than before from more than 1630 to 1304
        assertEquals(1626, amplifiedMethods.size()); // TODO
    }

    @Test
    public void testAvoidRedundantAmplification() throws Exception {

        /*
            This test implements the example cases showed in https://github.com/STAMP-project/dspot/issues/454
         */

        CtClass<?> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        final String nameMethod = "methodString";
        final CtMethod method = Utils.findMethod(literalMutationClass, nameMethod);
        final CtMethod clone = method.clone();
        clone.setSimpleName("temporaryMethod");
        clone.setBody(Utils.getFactory().createCodeSnippetStatement("int x = 1 + 1").compile());
        Amplifier zeroAmplifier = new AbstractLiteralAmplifier<Integer>() {
            @Override
            protected Set<CtExpression<Integer>> amplify(CtExpression<Integer> original, CtMethod<?> testMethod) {
                return Collections.singleton(testMethod.getFactory().createLiteral(0));
            }
            @Override
            protected String getSuffix() {
                return "zero-amplifier";
            }
            @Override
            protected Class<?> getTargetedClass() {
                return Integer.class;
            }
        };
        literalMutationClass.addMethod(clone);

        // used to verify that the application of Amplifiers does not modify the given test method
        final String originalTestMethodString = clone.toString();

        List<CtMethod<?>> zeroAmplifiedTests = zeroAmplifier.amplify(clone, 0).collect(Collectors.toList());
        assertEquals(2, zeroAmplifiedTests.size());
        assertEquals(originalTestMethodString, clone.toString()); // the original test method has not been modified
        zeroAmplifiedTests = zeroAmplifiedTests.stream().flatMap(testMethod -> zeroAmplifier.amplify(testMethod, 0)).collect(Collectors.toList());
        assertEquals(originalTestMethodString, clone.toString());
        assertEquals(1, zeroAmplifiedTests.size());
        literalMutationClass.removeMethod(clone);
    }
}
