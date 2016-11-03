package fr.inria.diversify.dspot.dynamic.invocation;

import fr.inria.diversify.dspot.dynamic.objectInstanciationTree.ObjectInstantiationBuilder;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.factory.Factory;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Simon
 * Date: 28/09/16
 * Time: 14:29
 */
public class ObjectValue implements Value {
    protected Map<String, PrimitiveValue> values;
    protected String dynamicType;
    protected Factory factory;

    public ObjectValue(String dynamicType, Factory factory) {
        values = new HashMap<>();
        this.factory = factory;
        this.dynamicType = dynamicType;
    }

    public void addValue(String fieldOrMethod, PrimitiveValue value) {
        values.put(fieldOrMethod, value);
    }

    @Override
    public String getDynamicType() {
        return dynamicType;
    }

    @Override
    public void initLocalVar(CtLocalVariable localVar, ObjectInstantiationBuilder objectInstantiationBuilder) throws Exception {
        CtBlock body = localVar.getParent(CtBlock.class);
        CtExpression constructorCall = objectInstantiationBuilder.findConstructorCall(factory.Class().get(dynamicType));
        if(constructorCall != null) {
            localVar.setAssignment(constructorCall);
            int count = 1;
            for (Map.Entry<String, PrimitiveValue> entry : values.entrySet()) {
                CtCodeSnippetStatement stmt;
                if (entry.getKey().endsWith("(")) {
                    stmt = factory.Code().createCodeSnippetStatement(localVar.getSimpleName() + "." + entry.getKey() + entry.getValue().simplePrintValue() + ")");
                } else {
                    stmt = factory.Code().createCodeSnippetStatement(localVar.getSimpleName() + "." + entry.getKey() + " = " + entry.getValue().simplePrintValue());
                }
                body.getStatements().add(count, stmt);
                count++;
            }
        } else {
            throw  new Exception();
        }
    }
}
