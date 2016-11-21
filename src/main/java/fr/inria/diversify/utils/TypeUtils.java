package fr.inria.diversify.utils;

import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.Map;

/**
 * User: Simon
 * Date: 23/08/16
 */
public class TypeUtils {

    public static boolean isSerializable(CtTypeReference type) {
        return isPrimitive(type)
                || isString(type)
                || isPrimitiveArray(type)
                || isPrimitiveCollection(type)
                || isPrimitiveMap(type);
    }

    public static boolean isPrimitive(CtTypeReference type) {
        try {
            return type.unbox().isPrimitive();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static  boolean isString(CtTypeReference type) {
        try {
            return String.class.isAssignableFrom(type.getActualClass());
        } catch (Exception e) {}
        return false;
    }

    public static boolean isPrimitiveArray(CtTypeReference type) {
        if(CtArrayTypeReference.class.isInstance(type)) {
            return isPrimitive(((CtArrayTypeReference)type).getComponentType());
        }
        return false;
    }

    public static boolean isPrimitiveCollection(CtTypeReference type) {
        try {
            return Collection.class.isAssignableFrom(type.getActualClass());
        } catch (Exception e) {}
        return false;
    }

    public static boolean isPrimitiveMap(CtTypeReference type) {
        try {
            return Map.class.isAssignableFrom(type.getActualClass());
        } catch (Exception e) {}
        return false;
    }
}
