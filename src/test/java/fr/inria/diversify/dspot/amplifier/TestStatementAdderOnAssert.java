package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.AbstractTest;
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
public class TestStatementAdderOnAssert extends AbstractTest {

    @Test
    public void testStatementAdderOnAssertLiteral() throws Exception, InvalidSdkException {

        /*
            The StatementAdderOnAssert will for each accessible method, try to build 3 kinds of parameters:
                - null -> only for not primitive value: "int a = (int)null;" is not compilable and worthless do so
                - a simple value: 1
                - a random value.
             mutation.ClassUnderTestTest got 2 accessible methods.
             The amplification will use the existing ClassUnderTest in the test and will also instantiate a new one to
             call the methods. Two new ClassUnderTest will be created: one null and one with the default constructor.
             The amplification results with 12 methods.
        */

        Factory factory = Utils.getFactory();
        CtClass<Object> ctClass = factory.Class().get("fr.inria.mutation.ClassUnderTestTest");
        AmplificationHelper.setSeedRandom(23L);

        StatementAdderOnAssert amplificator = new StatementAdderOnAssert();
        amplificator.reset(ctClass);

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

        assertEquals(2 * 2 * 3, amplifiedMethods.size());

        CtMethod currentMethod = amplifiedMethods.get(0);
        assertEquals("1", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertEquals("(fr.inria.mutation.ClassUnderTest)null", ((CtLocalVariable)(currentMethod.getBody().getStatement(2))).getDefaultExpression().toString()); // <- nullify the object
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("// StatementAdderMethod cloned existing statement\nvc_0.minusOne(int_vc_0)", currentMethod.getBody().getStatement(3).toString());

        currentMethod = amplifiedMethods.get(1);
        assertEquals("156591366", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(3) instanceof  CtInvocation);
        assertEquals("// StatementAdderMethod cloned existing statement\nvc_0.minusOne(vc_2)", currentMethod.getBody().getStatement(3).toString());

        currentMethod = amplifiedMethods.get(2);
        assertEquals("1", ((CtLocalVariable)(currentMethod.getBody().getStatement(1))).getDefaultExpression().toString());
        assertTrue(currentMethod.getBody().getStatement(1) instanceof  CtLocalVariable);
        assertTrue(currentMethod.getBody().getStatement(2) instanceof  CtInvocation);
        assertEquals("// StatementAdderMethod cloned existing statement\nunderTest.minusOne(int_vc_0)", currentMethod.getBody().getStatement(2).toString());

    }

}
