package fr.inria.diversify.profiling.processor.test;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

import java.util.ArrayList;
import java.util.List;

import static fr.inria.diversify.dspot.AmplificationChecker.isAssert;

public class AssertionRemover extends TestProcessor {
    protected String testDir;
    public static int monitorPointCount = 0;

	/*
     * This processor removes all the assertions from a test case
	 * For future version: 
	 * - we should validate first whether the assertion contains a call to a method under test. If yes, we should extract it.
	 * */


    public AssertionRemover(String testDir) {
        this.testDir = testDir;
    }

    public boolean isToBeProcessed(CtMethod candidate) {
        return candidate.getPosition().toString().contains(testDir);
    }

    @Override
    public void process(CtMethod method) {

        List<CtInvocation> stmts = Query.getElements(method, new TypeFilter(CtInvocation.class));
        for (CtInvocation invocation : stmts) {
            try {
                if (isAssert(invocation)) {
                    if (invocation.getParent() instanceof CtCase) {
                        CtCase ctCase = (CtCase) invocation.getParent();
                        int index = ctCase.getStatements().indexOf(invocation);
                        getArgs(invocation).forEach(arg -> {
                            if (!(arg instanceof CtVariableAccess) && !(arg instanceof CtFieldAccess)) {
                                ctCase.getStatements().add(index, buildVarStatement(arg));
                            }
                        });
                        ctCase.getStatements().remove(invocation);
                    } else {
                        CtBlock block = invocation.getParent(CtBlock.class);
                        getArgs(invocation).forEach(arg -> {
                            if (!(arg instanceof CtVariableAccess)) {
                                invocation.insertBefore(buildVarStatement(arg));
                            }
                        });
                        //removeStatement(block, invocation);
                        block.removeStatement(invocation);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    protected void removeStatement(CtBlock parent, CtStatement stmt) {
        int i = 0;
        List<CtStatement> stmts = parent.getStatements();
        for (; i < stmts.size(); i++) {
            if (stmts.get(i) == stmt) {
                break;
            }
        }
        stmts.remove(i);
    }

    protected CtCodeSnippetStatement buildVarStatement(CtElement arg) {
        CtCodeSnippetStatement stmt = new CtCodeSnippetStatementImpl();
        stmt.setValue("Object o" + monitorPointCount + " = " + arg.toString());
        monitorPointCount++;
        return stmt;
    }

    private List<CtElement> getArgs(CtInvocation invocation) {
        List<CtElement> list = new ArrayList<>();
        for (Object arg : invocation.getArguments()) {
            if (!(arg instanceof CtLiteral)) {
                CtElement i = (CtElement) arg;
                list.add(i);
            }
        }
        return list;
    }
}