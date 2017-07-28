package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.dspot.AbstractTest;
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
            It can reuse return value to add method call. It results here with 7 new test cases.
         */

        final String packageName = "fr.inria.statementadd";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(23L);
        StatementAdd amplifier = new StatementAdd(inputProgram.getFactory(), packageName);
        amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        System.out.println(amplifiedMethods);

        assertEquals(7, amplifiedMethods.size());

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

        assertEquals(expectedBody3, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd5".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString());
    }

    private final String expectedBody1 = "{" + nl +
            "    double d_7 = 0.5565331726524437;" + nl +
            "    float f_6 = 0.5079859F;" + nl +
            "    long l_5 = -582027917L;" + nl +
            "    int i_4 = -1918579922;" + nl +
            "    short s_3 = -28275;" + nl +
            "    byte by_2 = -74;" + nl +
            "    boolean b_1 = true;" + nl +
            "    char c_0 = 'h';" + nl +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl +
            "    clazz.methodWithReturn();" + nl +
            "    clazz.methodWithPrimitifParameters(c_0, b_1, by_2, s_3, i_4, l_5, f_6, d_7);" + nl +
            "}";

    private final String expectedBody2 = "{" + nl +
            "    java.lang.Double d_15 = 0.4633717339641672;" + nl +
            "    java.lang.Float f_14 = 0.3820523F;" + nl +
            "    java.lang.Long l_13 = 362573722L;" + nl +
            "    java.lang.Integer i_12 = 1517406316;" + nl +
            "    java.lang.Short s_11 = 16425;" + nl +
            "    java.lang.Byte by_10 = 10;" + nl +
            "    java.lang.Boolean b_9 = false;" + nl +
            "    java.lang.String str_8 = \"H_,y(q2 5[gpbL[{$QV5\";" + nl +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl +
            "    clazz.methodWithReturn();" + nl +
            "    clazz.methodWithPrimitifParameters(str_8, b_9, by_10, s_11, i_12, l_13, f_14, d_15);" + nl +
            "}";

    private final String expectedBody3 = "{" + nl +
            "    fr.inria.statementadd.ClassParameterAmplify object_16 = new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(776558984)), -2098187935);" + nl +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl +
            "    clazz.methodWithReturn();" + nl +
            "    clazz.methodWithDomainParameter(object_16);" + nl +
            "}";

}
