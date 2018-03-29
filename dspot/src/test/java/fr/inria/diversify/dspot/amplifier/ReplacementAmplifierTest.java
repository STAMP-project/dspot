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
    public void testOnLoop() throws Exception {
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
        final ReplacementAmplifier replacementAmplifier = new ReplacementAmplifier();
        final CtMethod originalTest = Utils.findMethod("fr.inria.factory.FactoryTest", "test");
        final List<CtMethod> test = replacementAmplifier.apply(originalTest);
        final String expectedMethod = "@org.junit.Test\n" +
                "public void test_replacement1() throws java.lang.Exception {\n" +
                "    final fr.inria.factory.FactoryTest.aClass aClass = build(-1183186497);\n" +
                "}";
        assertEquals(expectedMethod, test.get(0).toString());
    }
}
