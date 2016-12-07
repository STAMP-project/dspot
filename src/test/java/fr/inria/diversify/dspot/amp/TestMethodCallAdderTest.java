package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.Utils;
import org.junit.Test;
import spoon.Launcher;
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
public class TestMethodCallAdderTest {

    @Test
    public void testMethodCallAdd() throws Exception {

        /*
            Test that we duplicate method call in a test for each used method in the test.
                3 method are called in the original test, we produce 3 test methods.
         */

        Launcher launcher = Utils.buildSpoon();
        CtClass<Object> testClass = launcher.getFactory().Class().get("mutation.ClassUnderTestTest");

        TestMethodCallAdder methodCallAdder = new TestMethodCallAdder();
        AmplifierHelper.reset();

        final CtMethod<?> originalMethod = testClass.getMethods().stream().filter(m -> "testAddCall".equals(m.getSimpleName())).findFirst().get();
        List<CtMethod> amplifiedMethods = methodCallAdder.apply(originalMethod);

        assertEquals(3, amplifiedMethods.size());

        for (int i = 0 ; i < amplifiedMethods.size() ; i++) {
            CtMethod amplifiedMethod = amplifiedMethods.get(i);
            assertEquals(originalMethod.getBody().getStatements().size() + 1, amplifiedMethod.getBody().getStatements().size());
            CtStatement expectedStatement = originalMethod.getBody().getStatements().get(i+1);//+1 to skip the construction statement.
            assertEquals(expectedStatement, amplifiedMethod.getBody().getStatements().get(i+1));
            assertEquals(expectedStatement, amplifiedMethod.getBody().getStatements().get(i+2));
        }
    }
}
