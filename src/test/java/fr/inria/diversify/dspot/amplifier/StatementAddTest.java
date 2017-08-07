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
    public void testOnClassWithJavaObjects() throws Exception {

        /*
            Test that the StatementAdd amplifier is able to generate, and manage Collection and Map from java.util
         */

        final String packageName = "fr.inria.statementadd";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(32L);
        StatementAdd amplifier = new StatementAdd(packageName);
        amplifier.reset(factory.Class().get(packageName + ".ClassTarget"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTarget"), "test");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        System.out.println(amplifiedMethods);

        final String expectedBody1 = "{\n" +
                "    java.util.Map<fr.inria.statementadd.ClassParameterAmplify, java.lang.String> map_1 = java.util.Collections.singletonMap(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(-2078357828))), -575613422))), 221178161), \"@|,nBRih;>(&RC0P(.Ub\");\n" +
                "    fr.inria.statementadd.ClassTarget clazz = new fr.inria.statementadd.ClassTarget();\n" +
                "    clazz.getSizeOfTypedMap(map_1);\n" +
                "}";

        assertEquals(expectedBody1, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd4".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );
        final String expectedBody2 = "{\n" +
                "    java.util.Set set_18 = java.util.Collections.emptySet();\n" +
                "    fr.inria.statementadd.ClassTarget gen_o0 = new fr.inria.statementadd.ClassTarget();\n" +
                "    fr.inria.statementadd.ClassTarget clazz = new fr.inria.statementadd.ClassTarget();\n" +
                "    clazz.getSizeOf(set_18);\n" +
                "}";

        assertEquals(expectedBody2, amplifiedMethods.stream()
                .filter(amplifiedMethod ->
                        "test_sd1_sd21".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );
    }

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
                        "test_sd6".equals(amplifiedMethod.getSimpleName())
                ).findFirst()
                .get()
                .getBody()
                .toString()
        );
    }

    private final String expectedBody = "{\n" +
            "    double d_8 = 0.3816430907807956;\n" +
            "    float f_7 = 0.45125723F;\n" +
            "    long l_6 = -415012931L;\n" +
            "    int i_5 = 392236186;\n" +
            "    short s_4 = 27326;\n" +
            "    byte by_3 = 109;\n" +
            "    boolean b_2 = false;\n" +
            "    char c_1 = '#';\n" +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();\n" +
            "    clazz.methodWithPrimitifParameters(c_1, b_2, by_3, s_4, i_5, l_6, f_7, d_8);\n" +
            "    clazz.methodWithReturn();\n" +
            "}";

}
