package fr.inria.diversify.utils.sosiefier;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;

@Deprecated
public class TestLogProcessor extends TestProcessor {
    public TestLogProcessor() {
    }

    public boolean isToBeProcessed(CtMethod candidate) {
        return this.isTest(candidate);
    }

    public void process(CtMethod element) {
        Factory factory = this.getFactory();
        CtTry ctTry = factory.Core().createTry();
        ctTry.setBody(element.getBody());
        String snippet;
        String testName;
        if(element.getModifiers().contains(ModifierKind.STATIC)) {
            testName = element.getPosition().getCompilationUnit().getMainType().getQualifiedName() + "." + element.getSimpleName();
            snippet = this.getLogName() + ".testIn(Thread.currentThread(), \"" + testName + "\")";
        } else {
            testName = element.getSimpleName();
            snippet = this.getLogName() + ".testIn(Thread.currentThread(),this, \"" + testName + "\")";
        }

        CtCodeSnippetStatement snippetStatement = factory.Code().createCodeSnippetStatement(snippet);
        element.getBody().insertBegin(snippetStatement);
        snippet = this.getLogName() + ".testOut(Thread.currentThread())";
        CtCodeSnippetStatement snippetFinish = factory.Code().createCodeSnippetStatement(snippet);
        CtBlock finalizerBlock = factory.Core().createBlock();
        finalizerBlock.addStatement(snippetFinish);
        ctTry.setFinalizer(finalizerBlock);
        CtBlock methodBlock = factory.Core().createBlock();
        methodBlock.addStatement(ctTry);
        element.setBody(methodBlock);
    }
}