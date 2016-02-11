package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtExecutable;

/**
 * User: Simon
 * Date: 17/06/15
 * Time: 10:05
 */
public class CatchProcessor extends AbstractLoggingInstrumenter<CtCatch> {

    public CatchProcessor(InputProgram inputProgram) {
        super(inputProgram);
    }

    @Override
    public boolean isToBeProcessed(CtCatch candidate) {
        return candidate.getParent(CtExecutable.class) != null;
    }

    @Override
    public void process(CtCatch element) {
        int methodId = methodId(getMethod(element));
        int localId = getLocalId(element);

        String snippet = getLogger() + ".writeCatch(Thread.currentThread(),\"" + methodId + "\",\""
                + localId + "\"," + element.getParameter().getSimpleName() + ")";

        CtCodeSnippetStatement snippetStmt = getFactory().Code().createCodeSnippetStatement(snippet);
        element.getBody().insertBegin(snippetStmt);
    }
}
