package fr.inria.diversify.info.processor;

import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;


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
        return stmt.getParent(CtExecutable.class) != null
                && stmt.getParent() instanceof CtBlock
                && !(stmt.toString().startsWith("this(") || stmt.toString().startsWith("super("));
    }

    @Override
    public void process(CtStatement stmt) {
        String id = stmt.getPosition().getCompilationUnit().getMainType().getQualifiedName() + ":" + stmt.getPosition().getLine();
//        String cl = stmt.getParent(CtType.class).getQualifiedName() + "."+stmt.getParent(CtExecutable.class).getSimpleName();
        String snippet = getLogger() + ".logStmt(Thread.currentThread(),\"" + id + "\")";

        stmt.insertBefore(getFactory().Code().createCodeSnippetStatement(snippet));
    }
}
