package fr.inria.diversify.profiling.processor.main;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.visitor.CtScanner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simon on 16/10/14.
 */
public class FieldReferenceVisitor extends CtScanner {
    protected CtExecutable method;
    protected Map<CtFieldReference,String> fields;

    public FieldReferenceVisitor(CtExecutable method) {
        fields = new HashMap<>();
        this.method = method;
    }




    public Map<CtFieldReference, String> getFields() {
        return fields;
    }

    @Override
    public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
        super.visitCtFieldRead(fieldRead);
        String string = fieldRead.toString();
        if(!string.startsWith("super")
                && (!string.contains(".") || string.contains("this."))
                && fieldRead.getParent(CtExecutable.class).equals(method)
                && !isFinalInConstructor(fieldRead)) {
            fields.put(((CtFieldReference) fieldRead.getVariable()), fieldRead.toString());
        }
    }

    @Override
    public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
       super.visitCtFieldWrite(fieldWrite);
        String string = fieldWrite.toString();
        if(!string.startsWith("super")
                && (!string.contains(".") || string.contains("this."))
                && fieldWrite.getParent(CtExecutable.class).equals(method)
                && !isFinalInConstructor(fieldWrite)) {
            fields.put(((CtFieldReference) fieldWrite.getVariable()), fieldWrite.toString());
        }
    }

    protected boolean isFinalInConstructor(CtFieldAccess fieldaccess) {
        return method instanceof CtConstructor
                && fieldaccess.getVariable().getDeclaration().getModifiers().contains(ModifierKind.FINAL);
    }

}
