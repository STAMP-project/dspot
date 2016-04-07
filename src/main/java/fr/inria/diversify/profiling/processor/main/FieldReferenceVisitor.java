package fr.inria.diversify.profiling.processor.main;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.visitor.CtScanner;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by Simon on 16/10/14.
 */
public class FieldReferenceVisitor extends CtScanner {
    protected CtExecutable method;
    protected Map<CtFieldRead, String> fieldReads;
    protected Map<CtFieldWrite, String> fieldWrites;

    public FieldReferenceVisitor(CtExecutable method) {
        fieldReads = new IdentityHashMap<>();
        fieldWrites = new IdentityHashMap<>();
        this.method = method;
    }

    public Map<CtFieldAccess, String> getFields() {
        Map<CtFieldAccess, String> fields = new IdentityHashMap<>();
        fields.putAll(fieldWrites);
        fields.putAll(fieldReads);
        return fields;
    }

    public Map<CtFieldRead, String> getFieldReads() {
        return fieldReads;
    }

    public Map<CtFieldWrite, String> getFieldWrites() {
        return fieldWrites;
    }

    @Override
    public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
        super.visitCtFieldRead(fieldRead);
        String string = fieldRead.toString();
        if(!string.startsWith("super")
//                && (!string.contains(".") || string.contains("this."))
//                && fieldRead.getParent(CtExecutable.class).equals(method)
//                && !isFinalInConstructor(fieldRead)
                ) {
            fieldReads.put(fieldRead, fieldRead.toString());
        }
    }

    @Override
    public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
       super.visitCtFieldWrite(fieldWrite);
        String string = fieldWrite.toString();
        if(!string.startsWith("super")
//                && (!string.contains(".") || string.contains("this."))
//                && fieldWrite.getParent(CtExecutable.class).equals(method)
//                && !isFinalInConstructor(fieldWrite)
                ) {
            fieldWrites.put( fieldWrite, fieldWrite.toString());
        }
    }

    protected boolean isFinalInConstructor(CtFieldAccess fieldaccess) {
        return method instanceof CtConstructor
                && fieldaccess.getVariable().getDeclaration().getModifiers().contains(ModifierKind.FINAL);
    }

}
