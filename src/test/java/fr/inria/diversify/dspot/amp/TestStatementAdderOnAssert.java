package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/24/16
 */
public class TestStatementAdderOnAssert {

    @Test
    public void testStatementAdderOnAssertLiteral() throws Exception, InvalidSdkException {

        /*
            The StatementAdderOnAssert will for each accessible method, try to build 3 kinds of parameters:
                - null
                - a simple value: 1
                - a random value.
             mutation.ClassUnderTestTest got 2 accessible methods.
             The amplification will use the existing ClassUnderTest in the test and will also instantiate a new one to
             call the methods.
             The amplification results with 12 methods.
        */

        AmplifierHelper.setSeedRandom(23L);

        Factory factory = Utils.getFactory();
        CtClass<Object> ctClass = factory.Class().get("fr.inria.mutation.ClassUnderTestTest");

        StatementAdderOnAssert amplificator = new StatementAdderOnAssert();
        amplificator.reset(null, ctClass);

        CtMethod originalMethod = Utils.findMethod(ctClass, "testLit");
        List<CtMethod> amplifiedMethods = amplificator.apply(originalMethod);

        amplifiedMethods.forEach(method -> {
            assertEquals(1, method.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class) {
                @Override
                public boolean matches(CtLocalVariable ctLocalVariable) {
                    return ctLocalVariable.getType().isPrimitive();
                }
            }).size());
        });

        assertEquals(2 * 3 * 2, amplifiedMethods.size());

        CtMethod currentMethod = amplifiedMethods.get(0);
        assertEquals("(int)null", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtInvocation);
        assertEquals("underTest.minusOne(vc_2)", currentMethod.getBody().getStatement(2).toString());

        currentMethod = amplifiedMethods.get(1);
        assertEquals("1", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtInvocation);
        assertEquals("underTest.minusOne(vc_0)", currentMethod.getBody().getStatement(2).toString());

        currentMethod = amplifiedMethods.get(2);
        assertEquals("825130495", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtInvocation);
        assertEquals("underTest.minusOne(vc_3)", currentMethod.getBody().getStatement(2).toString());

        currentMethod = amplifiedMethods.get(3);
        assertEquals("(int)null", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtLocalVariable);
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("vc_1.minusOne(vc_2)", currentMethod.getBody().getStatement(3).toString());

        currentMethod = amplifiedMethods.get(4);
        assertEquals("1", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtLocalVariable);
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("vc_1.minusOne(vc_0)", currentMethod.getBody().getStatement(3).toString());

        currentMethod = amplifiedMethods.get(5);
        assertEquals("825130495", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtLocalVariable);
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("vc_1.minusOne(vc_3)", currentMethod.getBody().getStatement(3).toString());

        currentMethod = amplifiedMethods.get(6);
        assertEquals("(int)null", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtInvocation);
        assertEquals("underTest.plusOne(vc_6)", currentMethod.getBody().getStatement(2).toString());

        currentMethod = amplifiedMethods.get(7);
        assertEquals("0", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtInvocation);
        assertEquals("underTest.plusOne(vc_1)", currentMethod.getBody().getStatement(2).toString());

        currentMethod = amplifiedMethods.get(8);
        assertEquals("312752620", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtInvocation);
        assertEquals("underTest.plusOne(vc_7)", currentMethod.getBody().getStatement(2).toString());

        currentMethod = amplifiedMethods.get(9);
        assertEquals("(int)null", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtLocalVariable);
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("vc_5.plusOne(vc_6)", currentMethod.getBody().getStatement(3).toString());

        currentMethod = amplifiedMethods.get(10);
        assertEquals("0", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtLocalVariable);
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("vc_5.plusOne(vc_1)", currentMethod.getBody().getStatement(3).toString());

        currentMethod = amplifiedMethods.get(11);
        assertEquals("312752620", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtLocalVariable);
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("vc_5.plusOne(vc_7)", currentMethod.getBody().getStatement(3).toString());
    }

    @AfterClass
    public static void tearDown() throws InvalidSdkException, Exception {
        FileUtils.forceDelete(Utils.getCompiler().getBinaryOutputDirectory());
        FileUtils.forceDelete(Utils.getCompiler().getSourceOutputDirectory());
        Utils.reset();
    }

}
