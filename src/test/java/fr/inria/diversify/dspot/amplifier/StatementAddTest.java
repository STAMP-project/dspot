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
    public void testStatementAddOnArrayObjects() throws Exception {
        final String packageName = "fr.inria.statementaddarray";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(32L);
        StatementAdd amplifier = new StatementAdd(inputProgram.getFactory(), packageName);
        amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        System.out.println(amplifiedMethods);

        assertEquals(6, amplifiedMethods.size());

        assertEquals(expectedBody_array, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd1".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );

        assertEquals(expectedBody_array1, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd4".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );
    }

    private static final String expectedBody_array = "{" + nl  +
            "    fr.inria.statementaddarray.ClassParameterAmplify[] array_0 = new fr.inria.statementaddarray.ClassParameterAmplify[]{ new fr.inria.statementaddarray.ClassParameterAmplify(new int[]{ -485610818 }) };" + nl  +
            "    fr.inria.statementaddarray.ClassTargetAmplify clazz = new fr.inria.statementaddarray.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithArrayParatemeterFromDomain(array_0);" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "}";

    private static final String expectedBody_array1 = "{" + nl  +
            "    fr.inria.statementaddarray.ClassParameterAmplify object_2 = new fr.inria.statementaddarray.ClassParameterAmplify(new int[0]);" + nl  +
            "    fr.inria.statementaddarray.ClassTargetAmplify clazz = new fr.inria.statementaddarray.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithDomainParameter(object_2);" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "}";

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

    private final String expectedBody1 = "{" + nl  +
            "    double d_7 = 0.9033722646721782;" + nl  +
            "    float f_6 = 0.665549F;" + nl  +
            "    long l_5 = -1255373459L;" + nl  +
            "    int i_4 = 1190043011;" + nl  +
            "    short s_3 = -17589;" + nl  +
            "    byte by_2 = -28;" + nl  +
            "    boolean b_1 = false;" + nl  +
            "    char c_0 = '<';" + nl  +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "    clazz.methodWithPrimitifParameters(c_0, b_1, by_2, s_3, i_4, l_5, f_6, d_7);" + nl  +
            "}";

    private final String expectedBody2 = "{" + nl  +
            "    java.lang.Double d_15 = 0.7512804067674601;" + nl  +
            "    java.lang.Float f_14 = 0.5874274F;" + nl  +
            "    java.lang.Long l_13 = 681416186L;" + nl  +
            "    java.lang.Integer i_12 = 739670425;" + nl  +
            "    java.lang.Short s_11 = 13439;" + nl  +
            "    java.lang.Byte by_10 = -14;" + nl  +
            "    java.lang.Boolean b_9 = false;" + nl  +
            "    java.lang.String str_8 = \"0(TDja_(!DkOIfD[$(nt\";" + nl  +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "    clazz.methodWithPrimitifParameters(str_8, b_9, by_10, s_11, i_12, l_13, f_14, d_15);" + nl  +
            "}";

    private final String expectedBody3 = "{" + nl  +
            "    fr.inria.statementadd.ClassParameterAmplify object_16 = new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(-1062961681), -864933426);" + nl  +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "    clazz.methodWithDomainParameter(object_16);" + nl  +
            "}";

}
