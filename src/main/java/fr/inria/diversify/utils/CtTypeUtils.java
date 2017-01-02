package fr.inria.diversify.utils;

import fr.inria.diversify.util.Log;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.Map;

/**
 * User: Simon
 * Date: 23/08/16
 */
public class CtTypeUtils {

    public static boolean isSerializable(CtTypeReference type) {
        return isPrimitive(type)
                || isString(type)
                || isPrimitiveArray(type)
                || isPrimitiveCollection(type)
                || isPrimitiveMap(type);
    }

    public static boolean isPrimitive(CtTypeReference type) {
        try {
            return type.isPrimitive();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static  boolean isString(CtTypeReference type) {
        try {
            return String.class.isAssignableFrom(type.getActualClass());
        } catch (Exception ignored) {
            Log.warn("Error during check isString on " + type);
        }
        return false;
    }

    public static boolean isPrimitiveArray(CtTypeReference type) {
        return CtArrayTypeReference.class.isInstance(type) && isPrimitive(((CtArrayTypeReference) type).getComponentType());
    }

    public static boolean isPrimitiveCollection(CtTypeReference type) {
        try {
            return Collection.class.isAssignableFrom(type.getActualClass());
        } catch (Exception ignored) {
            Log.warn("Error during check isPrimitiveCollection on " + type);
        }
        return false;
    }

    public static boolean isPrimitiveMap(CtTypeReference type) {
        try {
            return Map.class.isAssignableFrom(type.getActualClass());
        } catch (Exception ignored) {
            Log.warn("Error during check isPrimitiveMap on " + type);
        }
        return false;
    }
}
