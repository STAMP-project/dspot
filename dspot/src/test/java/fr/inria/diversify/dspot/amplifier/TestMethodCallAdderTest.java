package fr.inria.diversify.dspot.amplifier;

import fr.inria.Utils;
import fr.inria.AbstractTest;
import org.junit.Test;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/7/16
 */
public class TestMethodCallAdderTest extends AbstractTest {

    @Test
    public void testMethodCallAddAll() throws Exception {

        /*
            Test that we reuse method call in a test for each used method in the test.
                3 method are called in the original test, we produce 3 test methods.
         */

        CtClass<Object> testClass = Utils.getFactory().Class().get("fr.inria.mutation.ClassUnderTestTest");

        TestMethodCallAdder methodCallAdder = new TestMethodCallAdder();
        methodCallAdder.reset(testClass);

        final CtMethod<?> originalMethod = testClass.getMethods().stream().filter(m -> "testAddCall".equals(m.getSimpleName())).findFirst().get();
        List<CtMethod> amplifiedMethods = methodCallAdder.apply(originalMethod);

        assertEquals(2, amplifiedMethods.size());

        for (int i = 0; i < amplifiedMethods.size(); i++) {
            CtMethod amplifiedMethod = amplifiedMethods.get(i);
            assertEquals(originalMethod.getBody().getStatements().size() + 1, amplifiedMethod.getBody().getStatements().size());
            CtStatement expectedStatement = originalMethod.getBody().getStatements().get(i + 1);//+1 to skip the construction statement.
            assertEquals(expectedStatement.toString(),
                    amplifiedMethod.getBody().getStatements().get(i + 1).toString());
            assertEquals(expectedStatement.toString(),
                    amplifiedMethod.getBody().getStatements().get(i + 2).toString());
        }
    }

}
