package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.profiling.processor.ProcessorUtil;
import fr.inria.diversify.runner.InputProgram;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User: Simon
 * Date: 09/04/15
 */
public class BranchCoverageProcessor extends AbstractLoggingInstrumenter<CtExecutable> {
    List<String> methodsId;
    Map<Integer, Integer> blockIds;
    boolean addBodyBranch;


    public BranchCoverageProcessor(InputProgram inputProgram, String outputDir, boolean addBodyBranch) throws IOException {
        super(inputProgram);
        File file = new File(outputDir + "/log/");
        if(!file.exists()) {
            file.mkdirs();
        }
        methodsId = new ArrayList<>();
        blockIds = new HashMap<>();
        this.addBodyBranch = addBodyBranch;
    }

    @Override
    public boolean isToBeProcessed(CtExecutable method) {
        return method.getBody() != null
                && (method instanceof CtMethod || method instanceof CtConstructor);
    }

    @Override
    public void process(CtExecutable method) {
        int methodId  = methodId(method);

        String info = methodId + ";" + method.getReference().getDeclaringType().getQualifiedName() + "_" + method.getSignature().replace(" ", "_");

        if(addBodyBranch) {
            addBranchLogger(tryFinallyBody(method).getBody(),"b");
            info += ";b";
        }

        for(Object object : Query.getElements(tryFinallyBody(method), new TypeFilter(CtIf.class))) {
            CtIf ctIf = (CtIf) object;
            int branchId = idBranch(methodId);
            CtStatement stmt = ctIf.getThenStatement();
            if (!(stmt instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(stmt.getParent());
                block.addStatement(stmt);
                ctIf.setThenStatement(block);
            }
            addBranchLogger(ctIf.getThenStatement(),"t" + branchId);
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
            addBranchLogger(ctIf.getElseStatement(), "e" + branchId);
            info += ";e" + branchId;
        }

        for(Object object : Query.getElements(tryFinallyBody(method), new TypeFilter(CtCase.class))) {
            CtCase ctCase = (CtCase) object;
            int branchId = idBranch(methodId);
            addBranchLogger(ctCase, "s" + branchId);
            info += ";s" + branchId;
        }

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
            int branchId = idBranch(methodId);
            addBranchLogger((CtBlock) ctLoop.getBody(), "l" + branchId);
            info += ";l" + branchId;
        }
        for(Object object : Query.getElements(tryFinallyBody(method), new TypeFilter(CtCatch.class))) {
            CtCatch ctCatch = (CtCatch) object;
            CtStatement stmt = ctCatch.getBody();
            if (!(stmt instanceof CtBlock)) {
                CtBlock block = getFactory().Core().createBlock();
                block.setParent(stmt.getParent());
                block.addStatement(stmt);
                ctCatch.setBody(block);
            }
            int branchId = idBranch(methodId);
            addBranchLogger((CtBlock)ctCatch.getBody(), "c" +branchId );
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

    protected int idBranch(int methodId) {
        if(!blockIds.containsKey(methodId)) {
            blockIds.put(methodId, 0);
        }
        blockIds.put(methodId, blockIds.get(methodId) + 1);
        return blockIds.get(methodId);
    }
}
