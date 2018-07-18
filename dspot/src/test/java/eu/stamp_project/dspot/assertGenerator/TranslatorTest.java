package eu.stamp_project.dspot.assertGenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import org.junit.Test;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/07/18
 */
public class TranslatorTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        final String statementToBeTranslated =
                "((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt()";
        final CtCodeSnippetStatement codeSnippetStatement = Utils.getFactory().createCodeSnippetStatement(statementToBeTranslated);
        final CtClass testClass = Utils.findClass("fr.inria.multipleobservations.TestClassToBeTest");
        final CtMethod method = Utils.findMethod(testClass, "test");
        method.getBody().insertEnd(codeSnippetStatement);
        testClass.compileAndReplaceSnippets();
        System.out.println(method);
    }

    @Test
    public void testFindDeepestElement() throws Exception {
        String statementToBeTranslated =
                "((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt()";
        final Translator translator = new Translator(Utils.getFactory());
        System.out.println(translator.buildInvocationFromString(statementToBeTranslated));
        statementToBeTranslated =
                "((fr.inria.multipleobservations.ClassToBeTest)((fr.inria.multipleobservations.ClassToBeTest)classToBeTest).getInt()).getInt()";
        System.out.println(translator.buildInvocationFromString(statementToBeTranslated));
    }
}
