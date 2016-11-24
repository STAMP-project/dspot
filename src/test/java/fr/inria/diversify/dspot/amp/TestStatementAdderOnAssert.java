package fr.inria.diversify.dspot.amp;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

/**
 * Created by Benjamin DANGLOT (benjamin.danglot@inria.fr) on 11/24/16.
 */
public class TestStatementAdderOnAssert {

    @Test
    public void testStatementAdderOnAssert() throws Exception {

        /*
            Adding statement in a class
        */
        Launcher launcher = buildSpoon();
        CtClass<Object> ctClassStatement = launcher.getFactory().Class().get("mutation.Statement");

        StatementAdderOnAssert amplificator = new StatementAdderOnAssert();
        amplificator.reset(null, null, ctClassStatement);

        List<CtMethod> methods = ctClassStatement.getElements(new TypeFilter<>(CtMethod.class));
    }

    private Launcher buildSpoon() {
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/resources/mutation/Statement.java");
        launcher.buildModel();
        return launcher;
    }
}
