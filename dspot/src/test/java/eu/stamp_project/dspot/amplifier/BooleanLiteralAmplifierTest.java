package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;

public class BooleanLiteralAmplifierTest extends AbstractTest {

    BooleanLiteralAmplifier amplifier;

    CtClass<Object> literalMutationClass;

    @Before
    public void setup() throws Exception {
        super.setUp();
        literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        amplifier = new BooleanLiteralAmplifier();
    }

    @Test
    public void testAmplify() {
        final String nameMethod = "methodBoolean";
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(1, mutantMethods.size());
    }

    @Test
    public void testAmplifyNull() {
        final String nameMethod = "methodNullBoolean";
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(2, mutantMethods.size());
    }
}
