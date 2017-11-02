package fr.inria.diversify.utils.sosiefier;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtExecutable;


/**
 * User: Simon
 * Date: 05/07/16
 * Time: 11:54
 */
@Deprecated
public class AddBlockEverywhereProcessor extends AbstractLoggingInstrumenter<CtStatement> {

    public AddBlockEverywhereProcessor(InputProgram inputProgram) {
        super(inputProgram);
    }

    @Override
    public boolean isToBeProcessed(CtStatement stmt) {
        return stmt.getParent(CtExecutable.class) != null;
    }

    public void process(CtStatement stmt) {
        if(stmt instanceof CtIf) {
            CtIf ctIf = (CtIf) stmt;
            CtStatement thenStmt = ctIf.getThenStatement();
            if (!(thenStmt instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(thenStmt.getParent());
                block.addStatement(thenStmt);
                ctIf.setThenStatement(block);
            }

            CtStatement elseStmt = ctIf.getElseStatement();
            if (elseStmt != null && !(elseStmt instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(stmt.getParent());
                block.addStatement(elseStmt);
                ctIf.setElseStatement(block);
            }
        }

        if(stmt instanceof CtLoop) {
            CtLoop ctLoop = (CtLoop) stmt;
            CtStatement loopBody = ctLoop.getBody();
            if (!(loopBody instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                if (loopBody != null) {
                    block.setParent(loopBody.getParent());
                    block.addStatement(loopBody);
                } else {
                    block.setParent(ctLoop);
                }
                ctLoop.setBody(block);
            }
        }
         if( stmt instanceof CtCatch) {
             CtCatch ctCatch = (CtCatch) stmt;
             CtStatement catchBody = ctCatch.getBody();
             if (!(catchBody instanceof CtBlock)) {
                 CtBlock block = getFactory().Core().createBlock();
                 block.setParent(catchBody.getParent());
                 block.addStatement(catchBody);
                 ctCatch.setBody(block);
             }
         }

        if(stmt instanceof CtCase) {
            CtCase ctCase = (CtCase) stmt;
            if(!(ctCase.getStatements().size() == 1 && ctCase.getStatements().get(0) instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(ctCase.getParent());

                ctCase.getStatements().stream()
                        .forEach(s ->  block.addStatement(s));
                block.getStatements().stream()
                        .forEach(s ->  ctCase.removeStatement(s));
                ctCase.addStatement(block);
            }
        }
    }
}
