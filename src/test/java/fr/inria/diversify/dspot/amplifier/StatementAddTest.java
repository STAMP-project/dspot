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
            "    double d_7 = 0.5855341934158772;" + nl  +
            "    float f_6 = 0.7848072F;" + nl  +
            "    long l_5 = 10L;" + nl  +
            "    int i_4 = 90;" + nl  +
            "    short s_3 = 43;" + nl  +
            "    byte by_2 = 18;" + nl  +
            "    boolean b_1 = false;" + nl  +
            "    char c_0 = 'k';" + nl  +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl  +
            "    clazz.methodWithReturn();" + nl  +
            "    clazz.methodWithPrimitifParameters(c_0, b_1, by_2, s_3, i_4, l_5, f_6, d_7);" + nl  +
            "}";

    private final String expectedBody2 = "{" + nl +
            "    java.lang.Double d_15 = 0.6860892400083877;" + nl +
            "    java.lang.Float f_14 = 0.9954207F;" + nl +
            "    java.lang.Long l_13 = 30L;" + nl +
            "    java.lang.Integer i_12 = 24;" + nl +
            "    java.lang.Short s_11 = 86;" + nl +
            "    java.lang.Byte by_10 = 81;" + nl +
            "    java.lang.Boolean b_9 = true;" + nl +
            "    java.lang.String str_8;" + nl +
            "    fr.inria.statementadd.ClassTargetAmplify clazz = new fr.inria.statementadd.ClassTargetAmplify();" + nl +
            "    clazz.methodWithPrimitifParameters(str_8, b_9, by_10, s_11, i_12, l_13, f_14, d_15);" + nl +
            "    clazz.methodWithReturn();" + nl +
            "}";

}
