package eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.assertionsyntaxbuilder;

import eu.stamp_project.dspot.AbstractTestOnSample;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/07/18
 */
public class TranslatorTest extends AbstractTestOnSample {

    @Override
    @Before
    public void setUp()  {
        super.setUp();
        // shared instance, the Translator does not have state between execution
        this.translatorUnderTest = new Translator(this.launcher.getFactory());
    }

    private Translator translatorUnderTest;

    private CtInvocation<?> translateInvocation(String statementToBeTranslated) {
        final CtExpression<?> translate = this.translatorUnderTest.translate(statementToBeTranslated);
        assertTrue(translate instanceof CtInvocation<?>);
        return (CtInvocation<?>) translate;
    }

    private CtVariableRead<?> translateVariableRead(String statementToBeTranslated) {
        final CtExpression<?> translate = this.translatorUnderTest.translate(statementToBeTranslated);
        assertTrue(translate instanceof CtVariableRead<?>);
        return (CtVariableRead<?>) translate;
    }

    @Test
    public void testWhenThrowingAStackOverFlowError() throws Exception {

        /*
           test that the Translator can handle the bug inside Spoon: getAllMethods() is throwing a StackOverFlowError.
           TODO this test should be temporary, until Spoon is fixed see https://github.com/INRIA/spoon/issues/2378
         */

        String statementToBeTranslated = "((fr.inria.overflow.Matriochka)object).aMethod()";
        CtInvocation<?> invocation = translateInvocation(statementToBeTranslated);
        assertEquals("((fr.inria.overflow.Matriochka) (object))", invocation.getTarget().toString());
        assertEquals("fr.inria.overflow.Matriochka", invocation.getTarget().getTypeCasts().get(0).toString());
        assertEquals("aMethod", invocation.getExecutable().getSimpleName());
    }

    @Test
    public void testOnUtilPackageSubClass() throws Exception {

        /*
            test the translation of a nested class from the java util package.
            Here the translator will use the classloader to find the class, then load it into the spoon factory
         */

        String statementToBeTranslated = "((java.util.AbstractMap.SimpleEntry)entry).toString()";
        CtInvocation<?> invocation = translateInvocation(statementToBeTranslated);
        assertEquals("((java.util.AbstractMap.SimpleEntry) (entry))", invocation.getTarget().toString());
        assertEquals("java.util.AbstractMap.SimpleEntry", invocation.getTarget().getTypeCasts().get(0).toString());
        assertEquals("toString", invocation.getExecutable().getSimpleName());
    }

    @Test
    public void testInvocationWithoutCast() throws Exception {
         /*
            test that the method return a well formed CtInvocation: target, typecast and executable
            on simple invocation, without cast
        */

        String statementToBeTranslated = "classToBeTest.isEmpty()";
        CtInvocation<?> invocation = translateInvocation(statementToBeTranslated);
        assertEquals("classToBeTest", invocation.getTarget().toString());
        assertTrue(invocation.getTarget().getTypeCasts().isEmpty());
        assertEquals("isEmpty", invocation.getExecutable().getSimpleName());
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
    public void testTranslateOnIsEmpty() throws Exception {

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

        statementToBeTranslated = "((java.util.List)list).isEmpty()";
        invocation = translateInvocation(statementToBeTranslated);
        assertEquals("((java.util.List) (list))", invocation.getTarget().toString());
        assertEquals("java.util.List", invocation.getTarget().getTypeCasts().get(0).toString());
        assertEquals("isEmpty", invocation.getExecutable().getSimpleName());
    }

    @Test
    public void testTranslateOnMethodFromSuperClass() throws Exception {

        /*
            Translator works on observed methods of super class

                The method test() is from the super class
         */


        String statementToBeTranslated = "((fr.inria.inheritance.Inherit)cl).test()";
        CtInvocation<?> invocation = translateInvocation(statementToBeTranslated);
        assertEquals("((fr.inria.inheritance.Inherit) (cl))", invocation.getTarget().toString());
        assertFalse(invocation.getTarget().getTypeCasts().isEmpty());
        assertEquals("test", invocation.getExecutable().getSimpleName());
    }
}
