package fr.inria.diversify.dspot.amp;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Created by Benjamin DANGLOT (benjamin.danglot@inria.fr) on 11/24/16.
 */
public class TestStatementAdderOnAssert {

    @Test
    public void testStatementAdderOnAssertLiteral() throws Exception {

        /*
            the returned amplified test set contains
        */

        Launcher launcher = buildSpoon();
        CtClass<Object> ctClass = launcher.getFactory().Class().get("mutation.ClassUnderTestTest");

        StatementAdderOnAssert amplificator = new StatementAdderOnAssert();
        amplificator.reset(null, null, ctClass);

        CtMethod originalMethod = ctClass.getElements(new TypeFilter<>(CtMethod.class)).get(0);
        List<CtMethod> amplifiedMethods = amplificator.apply(originalMethod);

        amplifiedMethods.forEach(method -> {
            assertEquals(1, method.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class) {
                @Override
                public boolean matches(CtLocalVariable ctLocalVariable) {
                    return ctLocalVariable.getType().isPrimitive();
                }
            }).size());
        });

        assertEquals(2 * 3, amplifiedMethods.size());
    }

    private Launcher buildSpoon() {
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/resources/mutation/ClassUnderTestTest.java");
        launcher.buildModel();
        return launcher;
    }
}
