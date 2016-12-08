package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.dspot.Utils;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
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
    public void testStatementAdderOnAssertLiteral() throws Exception {

        /*
            The StatementAdderOnAssert will for each accessible method, try to build 3 kinds of parameters:
                - null
                - a simple value: 1
                - a random value.
             mutation.ClassUnderTestTest got 3 accessible methods. The amplification results with 9 new test methods.
        */

        AmplifierHelper.setSeedRandom(23L);
        Launcher launcher = Utils.buildSpoon(Arrays.asList("src/test/resources/mutation/ClassUnderTestTest.java", "src/test/resources/mutation/ClassUnderTest.java"));
        CtClass<Object> ctClass = launcher.getFactory().Class().get("mutation.ClassUnderTestTest");

        StatementAdderOnAssert amplificator = new StatementAdderOnAssert();
        amplificator.reset(null, ctClass);

        CtMethod originalMethod = ctClass.getElements(new TypeFilter<>(CtMethod.class)).stream().filter(m -> "testLit".equals(m.getSimpleName())).findFirst().get();
        List<CtMethod> amplifiedMethods = amplificator.apply(originalMethod);

        amplifiedMethods.forEach(method -> {
            assertEquals(1, method.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class) {
                @Override
                public boolean matches(CtLocalVariable ctLocalVariable) {
                    return ctLocalVariable.getType().isPrimitive();
                }
            }).size());
        });

        assertEquals(3 * 3, amplifiedMethods.size());

        assertEquals("(int)null", ((CtLocalVariable)(amplifiedMethods.get(0).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(0).getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("0", ((CtLocalVariable)(amplifiedMethods.get(1).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(1).getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("825130495", ((CtLocalVariable)(amplifiedMethods.get(2).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(2).getBody().getStatement(3) instanceof  CtInvocation);

        assertEquals("(int)null", ((CtLocalVariable)(amplifiedMethods.get(3).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(3).getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("1", ((CtLocalVariable)(amplifiedMethods.get(4).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(4).getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("312752620", ((CtLocalVariable)(amplifiedMethods.get(5).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(5).getBody().getStatement(3) instanceof  CtInvocation);

        assertEquals("(int)null", ((CtLocalVariable)(amplifiedMethods.get(6).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(6).getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("0", ((CtLocalVariable)(amplifiedMethods.get(7).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(7).getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("-1918579922", ((CtLocalVariable)(amplifiedMethods.get(8).getBody().getStatement(2))).getDefaultExpression().toString());
        assertTrue(amplifiedMethods.get(8).getBody().getStatement(3) instanceof  CtInvocation);
    }


}
