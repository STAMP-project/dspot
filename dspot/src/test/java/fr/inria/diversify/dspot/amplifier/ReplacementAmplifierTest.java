package fr.inria.diversify.dspot.amplifier;

import fr.inria.Utils;
import fr.inria.AbstractTest;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReplacementAmplifierTest extends AbstractTest {

    @Test
    public void testReplacementOfArrayList() throws Exception {

        /*
            The replacement of an array list works properly
        */


        final String packageName = "fr.inria.statementadd";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(32L);
        ReplacementAmplifier amplifier = new ReplacementAmplifier();
        amplifier.reset(factory.Class().get(packageName + ".ClassTarget"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTarget"), "testWithLoop");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        assertEquals(1, amplifiedMethods.size());
        assertEquals("{" + AmplificationHelper.LINE_SEPARATOR +
                "    java.util.ArrayList<fr.inria.statementadd.TestClassTarget.Internal> internalList = new java.util.ArrayList<fr.inria.statementadd.TestClassTarget.Internal>(1891812663);" + AmplificationHelper.LINE_SEPARATOR +
                "    internalList.add(new fr.inria.statementadd.TestClassTarget.Internal());" + AmplificationHelper.LINE_SEPARATOR +
                "    for (fr.inria.statementadd.TestClassTarget.Internal i : internalList)" + AmplificationHelper.LINE_SEPARATOR +
                "        i.compute(0);" + AmplificationHelper.LINE_SEPARATOR +
                "" + AmplificationHelper.LINE_SEPARATOR +
                "}", amplifiedMethods.get(0).getBody().toString());
    }

    @Test
    public void testOnLoop() throws Exception {

        /*
            Test that the replacement amplifier does not replace local variable inside loops
         */

        final String packageName = "fr.inria.statementadd";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(32L);
        ReplacementAmplifier amplifier = new ReplacementAmplifier();
        amplifier.reset(factory.Class().get(packageName + ".ClassTarget"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTarget"), "testWithLoop");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        assertEquals(1, amplifiedMethods.size());
    }

    @Test
    public void test() throws Exception {

        /*
            Can use factory method to replace a value
         */

        final ReplacementAmplifier replacementAmplifier = new ReplacementAmplifier();
        final CtMethod originalTest = Utils.findMethod("fr.inria.factory.FactoryTest", "test");
        final List<CtMethod> test = replacementAmplifier.apply(originalTest);
        final String expectedMethod = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    final fr.inria.factory.FactoryTest.aClass aClass = build(-1183186497);" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethod, test.get(0).getBody().toString());
    }
}
