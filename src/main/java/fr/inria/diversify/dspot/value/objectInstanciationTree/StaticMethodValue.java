package fr.inria.diversify.dspot.value.objectInstanciationTree;

import fr.inria.diversify.dspot.value.Value;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.dspot.value.ValueType;
import spoon.reflect.code.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;


/**
 * User: Simon
 * Date: 23/11/16
 * Time: 17:34
 */
public class StaticMethodValue extends Value {

    private CtExecutableReference methodRef;

    public StaticMethodValue(ValueType type, CtExecutableReference methodRef, ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        this.type = type;
        this.methodRef = methodRef;
    }
    @Override
    public String getDynamicType() {
        return type.getType();
    }

    @Override
    public CtTypeReference getType() {
        return methodRef.getDeclaringType();
    }

    @Override
    public boolean isOk() {
        return true;
    }

    public CtExpression getValue() {
        Factory factory = methodRef.getFactory();
        CtTypeAccess target = factory.Code().createTypeAccess(getType());

        return factory.Code().createInvocation(target, methodRef);
    }

    @Override
    public void initLocalVar(CtBlock body, CtLocalVariable localVar) throws Exception {
        localVar.setDefaultExpression(getValue());
    }

}
