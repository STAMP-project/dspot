package fr.inria.diversify.dspot.value;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.reference.CtTypeReference;


/**
 * User: Simon
 * Date: 28/09/16
 * Time: 14:27
 */
public abstract class Value {
    protected ValueFactory valueFactory;
    protected ValueType type;

    public abstract String getDynamicType();

    public abstract CtTypeReference getType();

    public abstract boolean isOk();

    public abstract CtExpression getValue();

    public abstract void initLocalVar(CtBlock body, CtLocalVariable localVar) throws Exception;
}
