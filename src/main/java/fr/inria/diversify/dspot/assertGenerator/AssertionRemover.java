package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.dspot.AmplificationChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

import java.util.ArrayList;
import java.util.List;

import static fr.inria.diversify.dspot.AmplificationChecker.isAssert;

public class AssertionRemover extends AbstractProcessor<CtMethod> {

	private int counter;

	public AssertionRemover() {
		this.counter = 0;
	}

	/*
     * This processor removes all the assertions from a test case
	 * For future version: 
	 * - we should validate first whether the assertion contains a call to a method under test. If yes, we should extract it.
	 * */

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        return AmplificationChecker.isTest(candidate);
    }

    @Override
    public void process(CtMethod method) {
        List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
        for (CtInvocation invocation : invocations) {
            try {
                if (isAssert(invocation)) {
                    if (invocation.getParent() instanceof CtCase) {
                        CtCase ctCase = (CtCase) invocation.getParent();
                        int index = ctCase.getStatements().indexOf(invocation);
                        getArgs(invocation).forEach(arg -> {
                            if (!(arg instanceof CtVariableAccess)) {
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
                        block.removeStatement(invocation);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);//TODO checks if this try catch block is useful
            }
        }
    }

    private CtLocalVariable<Object> buildVarStatement(CtElement arg) {
		final CtLocalVariable<Object> localVariable = arg.getFactory().createLocalVariable();
		localVariable.setSimpleName("o" + counter++);
		localVariable.setType(arg.getFactory().createCtTypeReference(Object.class));
		localVariable.setDefaultExpression(arg.getFactory().createCodeSnippetExpression(arg.toString()));
		return localVariable;
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