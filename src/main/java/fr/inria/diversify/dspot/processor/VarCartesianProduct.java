package fr.inria.diversify.dspot.processor;

import fr.inria.diversify.codeFragment.Statement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 04/02/16
 * Time: 14:13
 */
public class VarCartesianProduct {
    List<CtVariableReference> oldVarRefs;
    List<List<CtVariableReference>> newVarsRefs;
    Map<CtVariableReference, Statement> newLocalVar;

    public VarCartesianProduct() {
        oldVarRefs = new ArrayList<>();
        newVarsRefs = new ArrayList<>();
        newLocalVar = new HashMap<>();
    }

    public void addReplaceVar(CtVariableReference oldVarRef, CtVariableReference newVarRef) {
        if(!oldVarRefs.contains(oldVarRef)) {
            oldVarRefs.add(oldVarRef);
            newVarsRefs.add(new ArrayList<>());
        }
        int index = oldVarRefs.indexOf(oldVarRef);
        newVarsRefs.get(index).add(newVarRef);
    }

    public void addReplaceVar(CtVariableReference oldVarRef, CtLocalVariable newVarDeclaration)  {
        if(!(isReceiver(oldVarRef) && isNull(newVarDeclaration))) {
            Factory factory = oldVarRef.getFactory();
            CtLocalVariableReference newVarRef = factory.Code().createLocalVariableReference(newVarDeclaration);
            Statement literalVar = new Statement(newVarDeclaration);
            newLocalVar.put(newVarRef, literalVar);
            addReplaceVar(oldVarRef, newVarRef);
        }
    }

    public void addReplaceVar(CtVariableReference oldVarRef, Statement cfLocalVar) {
        Factory factory = oldVarRef.getFactory();
        CtLocalVariable localVariable = (CtLocalVariable) cfLocalVar.getCtCodeFragment();
        CtLocalVariableReference newVarRef = factory.Code().createLocalVariableReference(localVariable);
        newLocalVar.put(newVarRef, cfLocalVar);
        addReplaceVar(oldVarRef, newVarRef);
    }


    public List<List<Statement>> apply(List<Statement> stmts, int targetIndex) {
        List<List<Statement>> codeFragmentsLists = new ArrayList<>();

        List<List<CtVariableReference>> cartesianProduct = cartesianProduct(newVarsRefs);

        for(List<CtVariableReference> list : cartesianProduct) {
            List<Statement> cloneStmts = cloneStmts(stmts);
            codeFragmentsLists.add(cloneStmts);
            Statement stmt = cloneStmts.get(targetIndex);

            for(int i = 0; i< oldVarRefs.size(); i++) {
                CtVariableReference oldVarRef = stmt.getInputContext().getVariableOrFieldNamed(oldVarRefs.get(i).getSimpleName());
                CtVariableReference newVarRef = list.get(i);

                if(newLocalVar.containsKey(newVarRef)) {
                    cloneStmts.add(0,newLocalVar.get(newVarRef));
                }
                oldVarRef.replace(newVarRef);
            }
        }
        return codeFragmentsLists;
    }

    protected  List<Statement> cloneStmts(List<Statement> stmts) {
        return stmts.stream()
                .map(stmt -> stmt.clone())
                .collect(Collectors.toList());
    }

    protected List<List<CtVariableReference>> cartesianProduct(List<List<CtVariableReference>> lists) {
        List<List<CtVariableReference>> resultLists = new ArrayList<>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<>());
            return resultLists;
        } else {
            List<CtVariableReference> firstList = lists.get(0);
            List<List<CtVariableReference>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (CtVariableReference condition : firstList) {
                for (List<CtVariableReference> remainingList : remainingLists) {
                    ArrayList<CtVariableReference> resultList = new ArrayList<>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    protected boolean isReceiver(CtVariableReference var) {
        CtInvocation invocation = var.getParent(CtInvocation.class);
        return invocation.getTarget() != null && invocation.getTarget().equals(var);
    }

    protected boolean isNull(CtLocalVariable localVariable) {
        return localVariable.getAssignment() == null || localVariable.getAssignment().toString().endsWith("null");
    }
}
