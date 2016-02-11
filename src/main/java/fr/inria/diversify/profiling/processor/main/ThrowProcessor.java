package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtExecutable;

/**
 * User: Simon
 * Date: 17/06/15
 * Time: 10:05
 */
public class ThrowProcessor extends AbstractLoggingInstrumenter<CtThrow> {

    public ThrowProcessor(InputProgram inputProgram) {
        super(inputProgram);
    }

    @Override
    public boolean isToBeProcessed(CtThrow candidate) {
        return candidate.getParent(CtExecutable.class) != null
                && !getMethod(candidate).getThrownTypes().isEmpty();
    }

    @Override
    public void process(CtThrow throwStmt) {
        int methodId = methodId(getMethod(throwStmt));
        int localId = getLocalId(throwStmt);

        String localVar = "throwable_" + localId;
        String snippet = throwStmt.getThrownExpression().getType() + " " + localVar + " = " + throwStmt.getThrownExpression();
        CtCodeSnippetStatement var = getFactory().Code().createCodeSnippetStatement(snippet);

        snippet =  getLogger()+".writeThrow(Thread.currentThread(),\"" + methodId + "\",\""
                + localId + "\"," + localVar + ")";
        CtCodeSnippetStatement log = getFactory().Code().createCodeSnippetStatement(snippet);

        CtCodeSnippetStatement thro = getFactory().Code().createCodeSnippetStatement("throw " + localVar);

        CtBlock block = getFactory().Core().createBlock();

        block.addStatement(var);
        block.addStatement(log);
        block.addStatement(thro);

        throwStmt.replace(block);
        throwStmt.getParent(CtBlock.class).removeStatement(throwStmt);
    }
}
