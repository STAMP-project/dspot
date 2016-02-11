package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Simon
 * Date: 21/05/15
 * Time: 14:31
 */
public class BranchPositionProcessor extends AbstractLoggingInstrumenter<CtExecutable> {
    List<String> methodsId;
    Map<Integer, Integer> blockIds;
    Map<String,SourcePosition> branchPosition;
    Map<String, String> branchConditionType;

    public BranchPositionProcessor(InputProgram inputProgram) {
        super(inputProgram);

        branchPosition = new HashMap<>();
        branchConditionType = new HashMap<>();
        methodsId = new ArrayList<>();
        blockIds = new HashMap<>();
    }

    @Override
    public boolean isToBeProcessed(CtExecutable method) {
        return method.getBody() != null
                && method.getPosition() != null;
    }

    @Override
    public void process(CtExecutable method) {
        int methodId = methodId(method);

        addBranch(methodId, "b", method.getBody());
        branchConditionType.put(methodId+".b", methodVisibility(method));

        for(Object object : Query.getElements(method, new TypeFilter(CtIf.class))) {
            CtIf ctIf = (CtIf) object;
            int branchId = idBranch(methodId);

            addBranch(methodId, "t" + branchId, ctIf.getThenStatement());
            updateBranchConditionType(ctIf, methodId+".t" + branchId);
            conditionType(ctIf.getCondition());
            if (ctIf.getElseStatement() == null) {
//                addBranch(methodId, "e" + branchId, ctIf.getParent(CtBlock.class));
//                updateBranchConditionType(ctIf,  methodId+".e" + branchId);
            } else {
                addBranch(methodId, "e" + branchId, ctIf.getElseStatement());
                updateBranchConditionType(ctIf,  methodId+".e" + branchId);
            }
        }

        for(Object object : Query.getElements(method, new TypeFilter(CtCase.class))) {
            CtCase ctCase = (CtCase) object;
            int branchId = idBranch(methodId);
            addBranch(methodId, "s" + branchId, ctCase);

        }

        for(Object object : Query.getElements(method, new TypeFilter(CtLoop.class))) {
            CtLoop ctLoop = (CtLoop) object;
            int branchId = idBranch(methodId);
            addBranch(methodId, "l" + branchId, ctLoop.getBody());
        }

        for(Object object : Query.getElements(method, new TypeFilter(CtCatch.class))) {
            CtCatch ctCatch = (CtCatch) object;
            int branchId = idBranch(methodId);
            addBranch(methodId, "c" + branchId, ctCatch.getBody());
        }
    }

    protected void addBranch(int methodId, String branchId, CtStatement blockOrStmt) {
        branchPosition.put(methodId + "." + branchId, blockOrStmt.getPosition());
    }

    protected int idBranch(int methodId) {
        if(!blockIds.containsKey(methodId)) {
            blockIds.put(methodId, 0);
        }
        blockIds.put(methodId, blockIds.get(methodId) + 1);
        return blockIds.get(methodId);
    }


    protected void updateBranchConditionType(CtElement element, String branchId) {
        if(element instanceof CtIf) {
            branchConditionType.put(branchId, conditionType(((CtIf) element).getCondition()));
        }
        if(element instanceof CtWhile) {
            branchConditionType.put(branchId, conditionType(((CtWhile) element).getLoopingExpression()));
        }
        if(element instanceof CtDo) {
            branchConditionType.put(branchId, conditionType(((CtDo) element).getLoopingExpression()));
        }
    }

    protected String conditionType(CtExpression condition) {
        List<CtBinaryOperator> binaryOperators = Query.getElements(condition, new TypeFilter(CtBinaryOperator.class));
        List<CtInvocation> methodCall = Query.getElements(condition, new TypeFilter(CtInvocation.class));

        if(binaryOperators.isEmpty()) {
            if(methodCall.isEmpty()) {
                return "boolean";
            } else {
                if(methodCall.contains("equal(")) {
                    return "equal";
                } else {
                    return "methodCall";
                }
            }
        } else {
            if(binaryOperators.size() == 1) {
                return binaryOperators.get(0).getKind().toString();
            } else {
                return "binaryOperator" + binaryOperators.size();
            }
        }
    }

    protected String methodVisibility(CtExecutable executable) {
        if(executable instanceof CtModifiable) {
            CtModifiable modifiable = (CtModifiable) executable;
            if(modifiable.getModifiers().contains(ModifierKind.PRIVATE)) {
                return "private";
            }
            if(modifiable.getModifiers().contains(ModifierKind.PROTECTED)) {
                return "protected";
            }
            if(modifiable.getModifiers().contains(ModifierKind.PUBLIC)) {
                return "public";
            }
            return "package-private";
        } else {
            return "none";
        }
    }

    public Map<String, SourcePosition> getBranchPosition() {
        return branchPosition;
    }

    public Map<String, String> getBranchConditionType() {
        return branchConditionType;
    }
}
