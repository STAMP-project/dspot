package fr.inria.diversify.utils.sosiefier;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * User: Simon
 * Date: 09/04/15
 */
@Deprecated
public class BranchCoverageProcessor extends AbstractLoggingInstrumenter<CtExecutable> {

    List<String> methodsId;
    boolean addBodyBranch;


    public BranchCoverageProcessor(InputProgram inputProgram, String outputDir, boolean addBodyBranch) throws IOException {
        super(inputProgram);
        File file = new File(outputDir + "/log/");
        if(!file.exists()) {
            file.mkdirs();
        }
        methodsId = new ArrayList<>();
        this.addBodyBranch = addBodyBranch;
    }

    @Override
    public boolean isToBeProcessed(CtExecutable method) {
        return method.getBody() != null
                && (method instanceof CtMethod || method instanceof CtConstructor);
    }

    @Override
    public void process(CtExecutable method) {
        int methodId = ProcessorUtil.methodId(method);
        String info = methodId + ";" + ProcessorUtil.methodString(method);

        if(addBodyBranch) {
            addBranchLogger(tryFinallyBody(method).getBody(),"b");
            info += ";b";
        }

        int branchId = 0;
        for(Object object : Query.getElements(tryFinallyBody(method), new TypeFilter(CtIf.class))) {
            CtIf ctIf = (CtIf) object;
            CtStatement stmt = ctIf.getThenStatement();
            if (!(stmt instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(stmt.getParent());
                block.addStatement(stmt);
                ctIf.setThenStatement(block);
            }
            addBranchLogger(ctIf.getThenStatement(),"t" + branchId++);
            info += ";t" + branchId;
            if (ctIf.getElseStatement() == null) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(stmt.getParent());
                ctIf.setElseStatement(block);
            } else {
                stmt = ctIf.getElseStatement();
                if (!(stmt instanceof CtBlock)) {
                    CtBlock block = getFactory().Core().createBlock();
                    block.setParent(stmt.getParent());
                    block.addStatement(stmt);
                    ctIf.setElseStatement(block);
                }
            }
            addBranchLogger(ctIf.getElseStatement(), "e" + branchId++);
            info += ";e" + branchId;
        }

        branchId = 0;
        for(Object object : Query.getElements(tryFinallyBody(method), new TypeFilter(CtCase.class))) {
            CtCase ctCase = (CtCase) object;
            if(!(ctCase.getStatements().size() == 1 && ctCase.getStatements().get(0) instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(ctCase.getParent());
                ctCase.getStatements().stream()
                        .forEach(s ->  block.addStatement(s));
                block.getStatements().stream()
                        .forEach(s ->  ctCase.removeStatement(s));
                ctCase.addStatement(block);
            }

            addBranchLogger(ctCase, "s" + branchId++);
            info += ";s" + branchId;
        }

        branchId = 0;
        for(Object object : Query.getElements(tryFinallyBody(method), new TypeFilter(CtLoop.class))) {
            CtLoop ctLoop = (CtLoop) object;
            CtStatement stmt = ctLoop.getBody();
            if (!(stmt instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                if (stmt != null) {
                    block.setParent(stmt.getParent());
                    block.addStatement(stmt);
                } else {
                    block.setParent(ctLoop);
                }
                ctLoop.setBody(block);
            }
            addBranchLogger((CtBlock) ctLoop.getBody(), "l" + branchId++);
            info += ";l" + branchId;
        }

        branchId = 0;
        for(Object object : Query.getElements(tryFinallyBody(method), new TypeFilter(CtCatch.class))) {
            CtCatch ctCatch = (CtCatch) object;
            CtStatement stmt = ctCatch.getBody();
            if (!(stmt instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(stmt.getParent());
                block.addStatement(stmt);
                ctCatch.setBody(block);
            }
            addBranchLogger((CtBlock)ctCatch.getBody(), "c" +branchId++);
            info += ";c" + branchId;
        }

        addInOut(method, methodId);
        ProcessorUtil.addInfo(info);
    }

    protected void addBranchLogger(CtStatementList stmts, String idBranch) {
        String snippet = getLogger() + ".branch(Thread.currentThread(),\"" + idBranch + "\")";

        CtCodeSnippetStatement beginStmt = getFactory().Core().createCodeSnippetStatement();
        beginStmt.setValue(snippet);

        if(stmts.getStatements().isEmpty()) {
            stmts.addStatement(beginStmt);
        }  else {
            stmts.getStatements().add(0,beginStmt);
        }
    }

    protected void addBranchLogger(CtBlock block, String idBranch) {
        String snippet = getLogger() + ".branch(Thread.currentThread(),\"" + idBranch + "\")";

        CtCodeSnippetStatement beginStmt = getFactory().Core().createCodeSnippetStatement();
        beginStmt.setValue(snippet);
        block.insertBegin(beginStmt);
    }

    protected void addInOut(CtExecutable method, int id) {
        CtTry ctTry = tryFinallyBody(method);
        Factory factory = getFactory();

        CtCodeSnippetStatement beginStmt = factory.Code().createCodeSnippetStatement(getLogger() + ".methodIn(Thread.currentThread(),\"" + id + "\")");
        ctTry.getBody().insertBegin(beginStmt);

        CtCodeSnippetStatement stmt = factory.Code().createCodeSnippetStatement(getLogger() + ".methodOut(Thread.currentThread(),\"" + id + "\")");
        ctTry.getFinalizer().addStatement(stmt);
    }
}
