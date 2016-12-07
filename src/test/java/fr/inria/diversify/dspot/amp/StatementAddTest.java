package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.dspot.Utils;
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
public class StatementAddTest {

    @Test
    public void testStatementAdd() throws Exception {
        final String packageName = "statementadd";
        InputProgram inputProgram = Utils.getInputProgram();

        final Factory factory = Utils.getFactory(inputProgram);
        inputProgram.setFactory(factory);
        AmplifierHelper.setSeedRandom(23L);
        ValueFactory valueFactory = new ValueFactory(inputProgram);
        StatementAdd amplificator = new StatementAdd(inputProgram.getFactory(), valueFactory, packageName);
        amplificator.reset(null, factory.Class().get(packageName + ".ClassTargetAmplify"));

        CtMethod<?> ctMethod = Utils.getCtMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
//        CtMethod<?> ctMethod = factory.Class().get(packageName + ".TestClassTargetAmplify").getMethods().stream().findAny().get();
        List<CtMethod> amplifiedMethods = amplificator.apply(ctMethod);

        assertEquals(4, amplifiedMethods.size());

        amplifiedMethods.forEach(System.out::println);
    }



}
