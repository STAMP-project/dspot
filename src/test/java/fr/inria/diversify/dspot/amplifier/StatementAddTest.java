package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.runner.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/30/16
 */
public class StatementAddTest extends AbstractTest {

    @Test
    public void testStatementAdd() throws Exception, InvalidSdkException {

        /*
            Test the StatementAdd amplifier. It reuse existing object to add method call of accessible method.
            It can reuse return value to add method call. It results here with64 new test cases.
         */

        final String packageName = "fr.inria.statementadd";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(23L);
        ValueFactory valueFactory = new ValueFactory(inputProgram.getFactory());
        StatementAdd amplifier = new StatementAdd(inputProgram.getFactory(), valueFactory, packageName);
        amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        assertEquals(6, amplifiedMethods.size());

        assertEquals(expectedBody1, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd2".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );

        assertEquals(expectedBody2, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd3".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString());
    }

    private final String expectedBody1 = "{" + nl  +
            "    double d_6 = 0.7018696454992024;" + nl  +
            "    float f_5 = 0.0728184F;" + nl  +
            "    long l_4 = 90L;" + nl  +
            "    int i_3 = 43;" + nl  +
            "    short s_2 = 18;" + nl  +
            "    byte by_1 = 47;" + nl  +
            "    boolean b_0 = false;" + nl  +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "    clazz.methodWithPrimitifParameters(b_0, by_1, s_2, i_3, l_4, f_5, d_6);" + nl  +
            "}";

    private final String expectedBody2 = "{" + nl  +
            "    java.lang.Double d_13 = 0.1315393550111239;" + nl  +
            "    java.lang.Float f_12 = 0.4807895F;" + nl  +
            "    java.lang.Long l_11 = 24L;" + nl  +
            "    java.lang.Integer i_10 = 86;" + nl  +
            "    java.lang.Short s_9 = 81;" + nl  +
            "    java.lang.Byte by_8 = 70;" + nl  +
            "    java.lang.Boolean b_7 = true;" + nl  +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithPrimitifParameters(b_7, by_8, s_9, i_10, l_11, f_12, d_13);" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "}";

}
