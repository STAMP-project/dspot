package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.runner.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/30/16
 */
public class StatementAddTest extends AbstractTest {

    @Test
    public void testStatementAddOnArrayObjects() throws Exception {
        final String packageName = "fr.inria.statementaddarray";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(32L);
        StatementAdd amplifier = new StatementAdd(packageName);
        amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        System.out.println(amplifiedMethods);

        assertEquals(26, amplifiedMethods.size());

        assertEquals(expectedBody_array, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd3".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );
    }

    private static final String expectedBody_array = "{" + nl  +
            "    fr.inria.statementaddarray.ClassParameterAmplify[] array_0 = new fr.inria.statementaddarray.ClassParameterAmplify[]{ new fr.inria.statementaddarray.ClassParameterAmplify(new int[0]) , new fr.inria.statementaddarray.ClassParameterAmplify(new int[]{ -1808333051 , -1789290896 , 1960853583 , 1908190513 }) };" + nl  +
            "    fr.inria.statementaddarray.ClassTargetAmplify clazz = new fr.inria.statementaddarray.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithArrayParatemeterFromDomain(array_0);" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "}";

    @Test
    public void testStatementAddOnUnderTest() throws Exception {
        Factory factory = Utils.getFactory();
        CtClass<Object> ctClass = factory.Class().get("fr.inria.mutation.ClassUnderTestTest");
        AmplificationHelper.setSeedRandom(23L);

        StatementAdd amplificator = new StatementAdd();
        amplificator.reset(ctClass);

        CtMethod originalMethod = Utils.findMethod(ctClass, "testLit");

        List<CtMethod> amplifiedMethods = amplificator.apply(originalMethod);

        System.out.println(amplifiedMethods);

        assertEquals(10, amplifiedMethods.size());
    }

    @Test
    public void testStatementAdd() throws Exception, InvalidSdkException {

        /*
            Test the StatementAdd amplifier. It reuse existing object to add method call of accessible method.
            It can reuse return value to add method call. It results here with 7 new test cases.
         */

        final String packageName = "fr.inria.statementadd";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(42L);
        StatementAdd amplifier = new StatementAdd(packageName);
        amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        System.out.println(amplifiedMethods);

        assertEquals(31, amplifiedMethods.size());

        assertEquals(expectedBody, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd4".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );
    }

    private final String expectedBody = "{" + nl  +
            "    double d_7 = 0.6655489517945736;" + nl  +
            "    float f_6 = 0.27707848F;" + nl  +
            "    long l_5 = -248792245L;" + nl  +
            "    int i_4 = 1325939940;" + nl  +
            "    short s_3 = -16344;" + nl  +
            "    byte by_2 = -31;" + nl  +
            "    boolean b_1 = false;" + nl  +
            "    char c_0 = 'h';" + nl  +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithPrimitifParameters(c_0, b_1, by_2, s_3, i_4, l_5, f_6, d_7);" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "}";

}
