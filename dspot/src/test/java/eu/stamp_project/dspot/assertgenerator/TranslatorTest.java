package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/07/18
 */
public class TranslatorTest extends AbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // shared instance, the Translator does not have state between execution
        this.translatorUnderTest = new Translator(Utils.getFactory());
    }

    private Translator translatorUnderTest;

    @NotNull
    private CtInvocation<?> translateInvocation(String statementToBeTranslated) {
        final CtExpression<?> translate = this.translatorUnderTest.translate(statementToBeTranslated);
        assertTrue(translate instanceof CtInvocation<?>);
        return (CtInvocation<?>) translate;
    }

    @NotNull
    private CtVariableRead<?> translateVariableRead(String statementToBeTranslated) {
        final CtExpression<?> translate = this.translatorUnderTest.translate(statementToBeTranslated);
        assertTrue(translate instanceof CtVariableRead<?>);
        return (CtVariableRead<?>) translate;
    }

    @Test
    public void testVariableRead() throws Exception {

        /*
            test the translation of variable read
         */

        String statementToBeTranslated = "classToBeTest";
        final CtVariableRead<?> ctVariableRead = translateVariableRead(statementToBeTranslated);
        assertEquals("classToBeTest", ctVariableRead.getVariable().getSimpleName());
    }

    @Test
    public void testSimpleInvocation() throws Exception {

        /*
            test that the method return a well formed CtInvocation: target, typecast and executable
            on simple invocation
        */

        String statementToBeTranslated =
                "((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt()";
        CtInvocation<?> invocation = translateInvocation(statementToBeTranslated);
        assertEquals("((fr.inria.multipleobservations.ClassToBeTest) (classToBeTest))", invocation.getTarget().toString());
        assertEquals("fr.inria.multipleobservations.ClassToBeTest", invocation.getTarget().getTypeCasts().get(0).toString());
        assertEquals("getInt", invocation.getExecutable().getSimpleName());
    }

    @Test
    public void testChainedInvocation() throws Exception {

         /*
            test that the method return a well formed CtInvocation: target, typecast and executable
                on chained invocation
          */
        String statementToBeTranslated =
                "((fr.inria.multipleobservations.ClassToBeTest)((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt()).getInt()";
        CtInvocation<?> invocation = translateInvocation(statementToBeTranslated);
        assertEquals("((fr.inria.multipleobservations.ClassToBeTest) (((fr.inria.multipleobservations.ClassToBeTest) (classToBeTest)).getInt()))", invocation.getTarget().toString());
        assertEquals("fr.inria.multipleobservations.ClassToBeTest", invocation.getTarget().getTypeCasts().get(0).toString());
        assertEquals("getInt", invocation.getExecutable().getSimpleName());
        assertTrue(invocation.getTarget() instanceof CtInvocation);

        invocation = (CtInvocation<?>) invocation.getTarget();
        assertEquals("((fr.inria.multipleobservations.ClassToBeTest) (classToBeTest))", invocation.getTarget().toString());
        assertEquals("fr.inria.multipleobservations.ClassToBeTest", invocation.getTarget().getTypeCasts().get(0).toString());
        assertEquals("getInt", invocation.getExecutable().getSimpleName());
    }


    @Test
    public void testtranslateOnIsEmpty() throws Exception {

        /*
            The observations produces a special case for empty collection
                Here, we verify that we can translate this special case.
         */

        String statementToBeTranslated = "((fr.inria.sample.ClassWithBoolean)cl).getEmptyList().isEmpty()";
        CtInvocation<?> invocation = translateInvocation(statementToBeTranslated);
        assertEquals("((fr.inria.sample.ClassWithBoolean) (cl)).getEmptyList()", invocation.getTarget().toString());
        assertTrue(invocation.getTarget().getTypeCasts().isEmpty());
        assertEquals("isEmpty", invocation.getExecutable().getSimpleName());
        assertEquals("java.util.List", invocation.getExecutable().getDeclaringType().getQualifiedName()); // verify the consistency of the spoon node
        assertTrue(invocation.getTarget() instanceof CtInvocation);

        invocation = (CtInvocation<?>) invocation.getTarget();
        assertEquals("((fr.inria.sample.ClassWithBoolean) (cl))", invocation.getTarget().toString());
        assertEquals("fr.inria.sample.ClassWithBoolean", invocation.getTarget().getTypeCasts().get(0).toString());
        assertEquals("getEmptyList", invocation.getExecutable().getSimpleName());
    }
}
