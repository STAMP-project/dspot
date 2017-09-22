package fr.inria.diversify.sosiefier.processor;


import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;

/**
 * User: Simon
 * Date: 10/21/13
 * Time: 9:27 AM
 */
@Deprecated
public class TestLogProcessor extends TestProcessor {

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        return isTest(candidate);
    }

    @Override
    public void process(CtMethod element) {
        Factory factory =  getFactory();
        CtTry ctTry = factory.Core().createTry();
        ctTry.setBody(element.getBody());

        String snippet;
        if(element.getModifiers().contains(ModifierKind.STATIC)) {
            String testName = element.getPosition().getCompilationUnit().getMainType().getQualifiedName() + "." + element.getSimpleName();
            snippet = getLogName() + ".testIn(Thread.currentThread(), \"" + testName + "\")";
        } else {
            String testName = element.getSimpleName();
           snippet = getLogName() + ".testIn(Thread.currentThread(),this, \"" + testName + "\")";
        }

        CtCodeSnippetStatement snippetStatement = factory.Code().createCodeSnippetStatement(snippet);
        element.getBody().insertBegin(snippetStatement);

        snippet = getLogName() + ".testOut(Thread.currentThread())";
        CtCodeSnippetStatement snippetFinish = factory.Code().createCodeSnippetStatement(snippet);

        CtBlock finalizerBlock = factory.Core().createBlock();
        finalizerBlock.addStatement(snippetFinish);
        ctTry.setFinalizer(finalizerBlock);

        CtBlock methodBlock = factory.Core().createBlock();
        methodBlock.addStatement(ctTry);
        element.setBody(methodBlock);
    }
}
