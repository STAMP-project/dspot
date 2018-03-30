package fr.inria.diversify.dspot.amplifier;

import fr.inria.Utils;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.AbstractTest;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
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
    public void testWithLoop() throws Exception {

        /*
            Test that StatementAdd amplifier is able to add statement inside a loop if this loop has not brackets
         */

        final String packageName = "fr.inria.statementadd";
        InputProgram inputProgram = Utils.getInputProgram();
        final Factory factory = inputProgram.getFactory();
        inputProgram.setFactory(factory);
        AmplificationHelper.setSeedRandom(32L);
        StatementAdd amplifier = new StatementAdd(packageName);
        amplifier.reset(factory.Class().get(packageName + ".ClassTarget"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTarget"), "testWithLoop");
        List<CtMethod> amplifiedMethods = amplifier.apply(ctMethod);

        System.out.println(amplifiedMethods);

        assertEquals("@org.junit.Test(timeout = 10000)\n" +
                "public void testWithLoop_sd1() throws java.lang.Exception {\n" +
                "    java.util.ArrayList<fr.inria.statementadd.TestClassTarget.Internal> internalList = new java.util.ArrayList<>();\n" +
                "    internalList.add(new fr.inria.statementadd.TestClassTarget.Internal());\n" +
                "    for (fr.inria.statementadd.TestClassTarget.Internal i : internalList) {\n" +
                "        int __DSPOT_i_0 = -1167796541;\n" +
                "        i.compute(0);\n" +
                "        // StatementAdd: add invocation of a method\n" +
                "        i.compute(__DSPOT_i_0);\n" +
                "    }\n" +
                "}", amplifiedMethods.get(0).toString());
    }

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

        assertEquals(7, amplifiedMethods.size());
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

        assertEquals(5, amplifiedMethods.size());
    }

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

        assertEquals(2, amplifiedMethods.size());
    }

    @Test
    public void testStatementAdd() throws Exception {

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

        assertEquals(6, amplifiedMethods.size());
    }

}
