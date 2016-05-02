package fr.inria.diversify.info.processor;

import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtStatement;


/**
 * User: Simon
 * Date: 21/04/16
 * Time: 16:14
 */
public class LogAllStmtInstrumenter extends AbstractLoggingInstrumenter<CtStatement> {


    public LogAllStmtInstrumenter(InputProgram inputProgram) {
        super(inputProgram);
    }

    @Override
    public boolean isToBeProcessed(CtStatement stmt) {
        return stmt.getParent(CtStatement.class) == null;
    }

    @Override
    public void process(CtStatement stmt) {
        int localId = getLocalId(stmt);

        String snippet = getLogger() + ".logStmt(" + Thread.currentThread() + ",\"" + localId + "\")";

        stmt.insertBefore(getFactory().Code().createCodeSnippetStatement(snippet));
    }
}
