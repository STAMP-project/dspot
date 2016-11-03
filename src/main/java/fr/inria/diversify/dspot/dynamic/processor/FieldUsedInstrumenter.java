package fr.inria.diversify.dspot.dynamic.processor;

import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.profiling.processor.main.FieldReferenceVisitor;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Simon on 16/07/14.
 */
public class FieldUsedInstrumenter extends AbstractLoggingInstrumenter<CtExecutable> {
    protected int localVarCount = 0;
    public FieldUsedInstrumenter(InputProgram inputProgram) {
        super(inputProgram);
    }

    public boolean isToBeProcessed(CtExecutable executable) {
        return !(executable instanceof CtLambda) && !(executable instanceof  CtAnonymousExecutable);
    }

    public void process(CtExecutable mth) {
        localVarCount = 0;
        int methodId = ProcessorUtil.methodId(mth);
        FieldReferenceVisitor scanner = getFieldUsed(mth);

        Map<CtFieldWrite,String> write = scanner.getFieldWrites();
        for(CtFieldWrite field : write.keySet()) {
            if(!isSystemField(field) || !isStatic(field) || isPrimitiveField(field)) {
                instruAccessFields(field, false, methodId);
            }
        }
        Map<CtFieldRead,String> read = scanner.getFieldReads();
            for(CtFieldRead field : read.keySet()) {
                if(!isSystemField(field) || !isStatic(field) || isPrimitiveField(field)) {
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
            String fieldName = fieldAccess.getVariable().getSimpleName();
            if(CtThisAccess.class.isInstance(target)) {
                targetString = "this";
            }
            if(CtTypeAccess.class.isInstance(target)) {
                fieldName = target.getType().getQualifiedName() + "." + fieldName;
                targetString = "\"static("+target.getType().getQualifiedName()+")\"";
            }
            String snippet;
            if(isRead) {
                snippet = getLogger() + ".readField(Thread.currentThread(),\"" +
                        methodId + "\"," +
                        targetString + ",\"" +
                        fieldName + "\")";
            } else {
                snippet = getLogger() + ".writeField(Thread.currentThread(),\"" +
                        methodId + "\"," +
                        targetString + ",\"" +
                        fieldName + "\")";
            }
            if(!targetDeclarationInLoopCondition(target)) {
                if (!stmt.toString().startsWith("super(") && !stmt.toString().startsWith("this(")) {
                    stmt.insertBefore(getFactory().Code().createCodeSnippetStatement(snippet));
                } else {
                    stmt.insertAfter(getFactory().Code().createCodeSnippetStatement(snippet));
                }
            }
        } catch (Exception e) {}
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
        try {
            if (fieldAccess.getVariable().getSimpleName().equals("class")
                    || fieldAccess.getTarget().toString().equals("System")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
        protected boolean isPrimitiveField(CtFieldAccess fieldAccess) {
            try {
                return fieldAccess.getType().isPrimitive();
            } catch (Exception e) {
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

    protected boolean isStatic(CtFieldAccess fieldAccess) {
        CtField field = fieldAccess.getVariable().getDeclaration();
        try {
            if (field != null) {
                return field.getModifiers().contains(ModifierKind.STATIC) && !(field.getParent() instanceof CtInterface);
            } else {
                return Modifier.isStatic(fieldAccess.getVariable().getActualField().getModifiers());
            }
        } catch (Exception e) {
            return false;
        }
    }
}
