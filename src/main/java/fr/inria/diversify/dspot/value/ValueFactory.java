package fr.inria.diversify.dspot.value;

import fr.inria.diversify.dspot.value.objectInstanciationTree.ObjectInstantiation;
import fr.inria.diversify.dspot.value.objectInstanciationTree.PrimitiveForNewReader;
import fr.inria.diversify.log.LogReader;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.io.IOException;
import java.util.*;

/**
 * User: Simon
 * Date: 19/10/16
 * Time: 11:11
 */
public class ValueFactory {
    protected InputProgram inputProgram;
    protected Map<String, ValueType> valueTypes;


    public ValueFactory(InputProgram inputProgram) {
        this.inputProgram = inputProgram;
        this.valueTypes = new HashMap<>();

        buildObjectInstantiation();
    }

    public ValueFactory(InputProgram inputProgram, String logDir) throws IOException {
        this.inputProgram = inputProgram;
        this.valueTypes = new HashMap<>();
        ValueType.setFactory(inputProgram.getFactory());

        buildObjectInstantiation();
        buildPrimitiveValue(logDir);


    }

    public ValueType getValueType(String typeName) {
        if(!valueTypes.containsKey(typeName)) {
            valueTypes.put(typeName, new ValueType(typeName));
        }
        return valueTypes.get(typeName);
    }

    public ValueType getValueType(CtTypeReference typeRef) {
        return getValueType(typeRef.getQualifiedName());
    }

    public ObjectInstantiation getObjectInstantiation(CtExecutableReference constructor) {
        ValueType type = getValueType(constructor.getType());
        ObjectInstantiation value = new ObjectInstantiation(type, constructor, this);
        type.addValue(value);
        return value;
    }

    public PrimitiveValue getPrimitiveValue(ValueType type, String value) {
        PrimitiveValue primitiveValue = new PrimitiveValue(type, value);
        type.addValue(primitiveValue);

        return primitiveValue;
    }

    protected void buildPrimitiveValue(String logDir) throws IOException {
        LogReader logReader = new LogReader(logDir);
        PrimitiveForNewReader reader = new PrimitiveForNewReader(inputProgram, this);
        logReader.addParser(reader);
        logReader.readLogs();
    }

    protected Set<ObjectInstantiation> buildObjectInstantiation() {
        Set<ObjectInstantiation> objectInstantiations = new HashSet<>();
        Set<String> filter = new HashSet<>();
        List<CtConstructorCall> constructorCalls = inputProgram.getAllElement(CtConstructorCall.class);

        for (CtConstructorCall cc : constructorCalls) {
            String string = cc.getExecutable().toString();
            if (!filter.contains(string)) {
                filter.add(string);

                ValueType type = getValueType(cc.getType());
                objectInstantiations.add(new ObjectInstantiation(type, cc.getExecutable(), this));
            }
        }
        return objectInstantiations;
    }

    public CtExpression findConstructorCall(CtClass target, boolean withSubType) {
        if(withSubType) {
            CtTypeReference ref = target.getReference();
            return target.getFactory().Class().getAll(false).stream()
                    .filter(type -> type.getReference().isSubtypeOf(ref))
                    .filter(type -> type instanceof CtClass)
                    .map(cl -> findConstructorCall((CtClass) cl))
                    .filter(expression -> expression != null)
                    .findFirst()
                    .orElse(null);

        } else {
            return findConstructorCall(target);
        }
    }

    public CtExpression findConstructorCall(CtClass target) {
        if(target != null && target.isTopLevel() && !target.getModifiers().contains(ModifierKind.ABSTRACT)) {
            String className = target.getQualifiedName();
            ValueType valueType = getValueType(className);
            Value value = valueType.getRandomValue(true);

            if(value != null) {
                return value.getValue();
            }
        }
        return null;
    }
}
