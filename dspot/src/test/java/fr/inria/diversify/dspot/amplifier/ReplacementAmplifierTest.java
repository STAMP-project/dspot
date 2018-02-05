package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReplacementAmplifierTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        final ReplacementAmplifier replacementAmplifier = new ReplacementAmplifier();
        final CtMethod originalTest = Utils.findMethod("fr.inria.factory.FactoryTest", "test");
        final List<CtMethod> test = replacementAmplifier.apply(originalTest);
        final String expectedMethod = "@org.junit.Test\n" +
                "public void test() throws java.lang.Exception {\n" +
                "    final fr.inria.factory.FactoryTest.aClass aClass = build(-1183186497);\n" +
                "}";
        assertEquals(expectedMethod, test.get(0).toString());
    }
}
