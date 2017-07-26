package fr.inria.diversify.dspot.value;

import fr.inria.diversify.dspot.value.objectInstanciationTree.ObjectInstantiation;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

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
        ValueType.setFactory(inputProgram.getFactory(), this);
        buildObjectInstantiation();
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

    protected Set<ObjectInstantiation> buildObjectInstantiation() {
        Set<ObjectInstantiation> objectInstantiations = new HashSet<>();
        Set<String> filter = new HashSet<>();
        List<CtConstructorCall> constructorCalls = inputProgram.getAllElement(CtConstructorCall.class);

        for (CtConstructorCall cc : constructorCalls) {
            if (!isPrivate(cc)) {
                String string = cc.getExecutable().toString();
                if (!filter.contains(string)) {
                    filter.add(string);

                    ValueType type = getValueType(cc.getType());
                    objectInstantiations.add(new ObjectInstantiation(type, cc.getExecutable(), this));
                }
            }
        }
        return objectInstantiations;
    }

    public boolean isPrivate(CtConstructorCall cc) {
        if(cc.getExecutable() == null || cc.getExecutable().getDeclaration() == null) {
            return false;
        } else {
            return ((CtConstructor)cc.getExecutable().getDeclaration()).getModifiers().contains(ModifierKind.PRIVATE);
        }
    }

    public boolean hasConstructorCall(CtClass target, boolean withSubType) {
        if(withSubType) {
            CtTypeReference ref = target.getReference();
            return target.getFactory().Class().getAll(false).stream()
                    .filter(type -> type.getReference().isSubtypeOf(ref))
                    .filter(type -> type instanceof CtClass)
                    .map(cl -> hasConstructorCall((CtClass) cl))
                    .anyMatch(expression -> expression != null);

        } else {
            return hasConstructorCall(target);
        }
    }

    public boolean hasConstructorCall(CtClass target) {
        if(target != null && target.isTopLevel() && !target.getModifiers().contains(ModifierKind.ABSTRACT)) {
            String className = target.getQualifiedName();
            ValueType valueType = getValueType(className);
            Value value = valueType.getRandomValue(true);

            return value != null;
        }
        return false;
    }
}
