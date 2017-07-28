package fr.inria.diversify.dspot.value;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;

/**
 * User: Simon
 * Date: 19/10/16
 * Time: 11:11
 */
public class ValueFactory {

    private Map<CtTypeReference, ValueType> valueTypes;


    public ValueFactory(Factory factory) {
        this.valueTypes = new HashMap<>();
//        ValueType.setFactory(factory, this);
    }

    public ValueType getValueType(CtTypeReference typeRef) {
        if(!valueTypes.containsKey(typeRef)) {
            valueTypes.put(typeRef, new ValueType(typeRef));
        }
        return valueTypes.get(typeRef);
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
        return target != null && target.isTopLevel() && !target.getModifiers().contains(ModifierKind.ABSTRACT) &&
                (getValueType(target.getReference()).getRandomValue() != null);
    }
}
