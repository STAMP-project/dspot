package fr.inria.diversify.profiling.processor.test;

import fr.inria.diversify.profiling.processor.ProcessorUtil;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

import java.util.ArrayList;
import java.util.List;

public class AssertionRemover extends TestProcessor {
    protected String testDir;
    public static int monitorPointCount = 0;
    protected boolean logAssert;

	/*
	 * This processor removes all the assertions from a test case
	 * For future version: 
	 * - we should validate first whether the assertion contains a call to a method under test. If yes, we should extract it.
	 * */


    public AssertionRemover(String testDir, boolean logAssert) {
        this.testDir = testDir;
        this.logAssert = logAssert;
    }

	public boolean isToBeProcessed(CtMethod candidate) {
        return candidate.getPosition().toString().contains(testDir);
    }

    @Override
    public void process(CtMethod method) {

        List<CtInvocation> stmts = Query.getElements(method, new TypeFilter(CtInvocation.class));
        for(CtInvocation invocation: stmts){
            try {
                if (isAssert(invocation)) {
                    if(invocation.getParent() instanceof CtCase) {
                        CtCase ctCase = (CtCase) invocation.getParent();
                        int index = ctCase.getStatements().indexOf(invocation);
                        getArgs(invocation).stream()
                                           .forEach(arg -> {
                                               if(logAssert && !isArrayAccess(arg)) {
                                                   ctCase.getStatements().add(index, buildLogStatement(arg));
                                               } else {
                                                   if(!(arg instanceof CtVariableAccess) && !(arg instanceof CtFieldAccess)) {
                                                       ctCase.getStatements().add(index, buildVarStatement(arg));
                                                   }
                                               }
                                           });
                        ctCase.getStatements().remove(invocation);
                    } else {
                        CtBlock block = (CtBlock) invocation.getParent();

                        getArgs(invocation).stream()
                                           .forEach(arg -> {
                                               if(logAssert && !isArrayAccess(arg)) {
                                                   invocation.insertBefore(buildLogStatement(arg));
                                               } else {
                                                   if(!(arg instanceof CtVariableAccess) && !(arg instanceof CtFieldAccess)) {
                                                       invocation.insertBefore(buildVarStatement(arg));
                                                   }
                                               }
                                           });
                        block.removeStatement(invocation);
                    }
                }
            } catch (Exception e) {}

        }
        if(logAssert) {
            if (!method.getModifiers().contains(ModifierKind.STATIC)) {
                List<CtAssignment> assignments = Query.getElements(method, new TypeFilter(CtAssignment.class));
                for (CtAssignment assignment : assignments) {
                    if (!(assignment.getParent() instanceof CtLoop) && !isArrayAccess(assignment.getAssigned())) {
                        try {
                            assignment.insertAfter(logAssignment(assignment.getAssigned()));
                        } catch (Exception e) {
                        }

                    }
                }

                List<CtLocalVariable> vars = Query.getElements(method, new TypeFilter(CtLocalVariable.class));
                for (CtLocalVariable var : vars) {

                    if (var.getDefaultExpression() != null && !(var.getParent() instanceof CtLoop)) {
                        var.insertAfter(logLocalVar(var));
                    }
                }
            }
        }
    }

    protected CtCodeSnippetStatement buildVarStatement(CtElement arg) {
        CtCodeSnippetStatement stmt = new CtCodeSnippetStatementImpl();
        stmt.setValue("Object o" + monitorPointCount +  " = " + arg.toString());
        monitorPointCount++;
        return stmt;
    }

    protected CtCodeSnippetStatement logLocalVar(CtLocalVariable var) {
        int id = ProcessorUtil.idFor(var.getPosition().getLine() + "_" + var.getReference());
        return buildSnippet(id, var.getSimpleName());
    }


    protected CtCodeSnippetStatement logAssignment(CtElement expression) {
        int id = ProcessorUtil.idFor(expression.getPosition().getLine() + "_" + expression.toString());
        return buildSnippet(id, expression.toString());
    }

    protected CtCodeSnippetStatement buildSnippet(int id, String expression) {
        monitorPointCount++;
        CtCodeSnippetStatement stmt = new CtCodeSnippetStatementImpl();
        String snippet = getLogName() + ".logAssertArgument(Thread.currentThread()," + id + ","+ expression + ")";
        stmt.setValue(snippet);

        return stmt;
    }

	protected List<CtElement> getArgs(CtInvocation invocation) {
		List<CtElement> list = new ArrayList<>();
		for(Object arg : invocation.getArguments()) {
			if(!(arg instanceof CtLiteral)) {
				CtElement i = (CtElement)arg;
				list.add(i);
			}
		}
		return list;
	}

    protected boolean isArrayAccess(CtElement element) {
        return element instanceof CtArrayAccess;
    }

    protected CtCodeSnippetStatement buildLogStatement(CtElement arg) {
        int id = ProcessorUtil.idFor(arg.getPosition().getLine() + "_" + arg.toString());

        return buildSnippet(id, arg.toString());
    }
}