package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by Simon on 16/07/14.
 */
public class FieldUsedInstrumenter extends AbstractLoggingInstrumenter<CtExecutable> {
    protected int localVarCount = 0;
    public FieldUsedInstrumenter(InputProgram inputProgram) {
        super(inputProgram);
    }

    public void process(CtExecutable mth) {
        localVarCount = 0;
        int methodId = methodId(mth);
        FieldReferenceVisitor scanner = getFieldUsed(mth);

        Map<CtFieldWrite,String> write = scanner.getFieldWrites();
        for(CtFieldWrite field : write.keySet()) {
            if(!(isSystemField(field) || isStaticAndInit(field))) {
                instruAccessFields(field, false, methodId);
            }
        }
        Map<CtFieldRead,String> read = scanner.getFieldReads();
            for(CtFieldRead field : read.keySet()) {
                if(!(isSystemField(field) || isStaticAndInit(field))) {
                    instruAccessFields(field, true, methodId);
                }
            }
    }

    protected void instruAccessFields(CtFieldAccess fieldAccess, boolean isRead, int methodId) {
        try {
            CtStatement stmt = getParentStatement(fieldAccess);
            CtExpression target = fieldAccess.getTarget();

            if(!CtThisAccess.class.isInstance(target) && !Query.getElements(target, new TypeFilter(CtInvocation.class)).isEmpty()) {
                CtLocalVariable localVar = getFactory().Code().createLocalVariable(target.getType(), "spoon_var_"+localVarCount, target);
                localVarCount++;
                stmt.insertBefore(localVar);
                fieldAccess.setTarget(getFactory().Code().createVariableRead(getFactory().Code().createLocalVariableReference(localVar), false));
            }

            String targetString = target.toString();
            if(CtThisAccess.class.isInstance(target)) {
                targetString = "this";
            }
            if(fieldAccess.getVariable().getModifiers().contains(ModifierKind.STATIC)) {
                targetString = "\"static("+target.getType().getQualifiedName()+")\"";
            }
            String snippet;
            if(isRead) {
                snippet = getLogger() + ".readField(Thread.currentThread(),\"" +
                        methodId + "\"," +
                        targetString + ",\"" +
                        fieldAccess.getVariable().getSimpleName() + "\")";
            } else {
                snippet = getLogger() + ".writeField(Thread.currentThread(),\"" +
                        methodId + "\"," +
                        targetString + ",\"" +
                        fieldAccess.getVariable().getSimpleName() + "\")";
            }
            if(!targetDeclarationInLoopCondition(target)) {
                if (!stmt.toString().startsWith("super(") && !stmt.toString().startsWith("this(")) {
                    stmt.insertBefore(getFactory().Code().createCodeSnippetStatement(snippet));
                } else {
                    stmt.insertAfter(getFactory().Code().createCodeSnippetStatement(snippet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.debug("");
        }
    }

    protected boolean targetDeclarationInLoopCondition(CtExpression target) {
        if(target.getParent(CtLoop.class) != null && CtVariableRead.class.isInstance(target)) {
            CtVariableRead varRead = (CtVariableRead) target;
            if(CtLocalVariableReference.class.isInstance(varRead.getVariable())) {
                CtLoop loopParent = target.getParent(CtLoop.class);
                if (!Query.getElements(loopParent.getBody(), new TypeFilter(CtElement.class)).contains(varRead.getVariable().getDeclaration())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected FieldReferenceVisitor getFieldUsed(CtExecutable mth) {
        FieldReferenceVisitor scanner = new FieldReferenceVisitor(mth);
        mth.accept(scanner);
        return scanner;
    }

    protected CtStatement getParentStatement(CtElement element) {
        if(!(element.getParent() instanceof CtBlock)) {
            return  getParentStatement(element.getParent(CtStatement.class));
        }
        return (CtStatement) element;
    }

    protected boolean isSystemField(CtFieldAccess fieldAccess) {
        if(fieldAccess.getVariable().getSimpleName().equals("class")
                || fieldAccess.getTarget().toString().equals("System")) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isStaticAndInit(CtFieldAccess fieldAccess) {
        CtField field = fieldAccess.getVariable().getDeclaration();
        try {
            if (field != null) {
                return field.getModifiers().contains(ModifierKind.STATIC) && field.getAssignment() != null;
            } else {
                return Modifier.isStatic(fieldAccess.getVariable().getActualField().getModifiers());
            }
        } catch (Exception e) {
            return false;
        }
    }
}
