package eu.stamp_project.compare;

import java.util.*;

/**
 * User: Simon
 * Date: 23/03/16
 * Time: 15:48
 */
public class Utils {

    protected static Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isPrimitiveCollectionOrMap(Object collectionOrMap) {
        try {
            return collectionOrMap != null &&
                    (isNonEmptyPrimitiveCollection(collectionOrMap) || isNonEmptyPrimitiveMap(collectionOrMap));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCollection(Object object) {
        return Collection.class.isInstance(object);
    }

    public static boolean isNonEmptyPrimitiveCollection(Object object) {
        if (isCollection(object) && !((Collection) object).isEmpty()) {
            Collection collection = (Collection) object;
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next != null) {
                    return isPrimitive(next);
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isMap(Object object) {
        return Map.class.isInstance(object);
    }

    public static boolean isNonEmptyPrimitiveMap(Object object) {
        if (isMap(object) && !((Map) object).isEmpty()) {
            Map map = (Map) object;
            boolean isKeyPrimitive = true;
            boolean isValuePrimitive = true;
            Iterator keyIterator = map.keySet().iterator();
            while (keyIterator.hasNext()) {
                Object next = keyIterator.next();
                if (!isPrimitive(next)) {
                    isKeyPrimitive = false;
                    break;
                }
            }
            if (isKeyPrimitive) {
                Iterator valueIterator = map.values().iterator();
                while (valueIterator.hasNext()) {
                    Object next = valueIterator.next();
                    if (!isPrimitive(next)) {
                        isValuePrimitive = false;
                        break;
                    }
                }
            }
            return isKeyPrimitive && isValuePrimitive;
        }
        return false;
    }

    public static boolean isPrimitive(Object object) {
        return object != null && isPrimitive(object.getClass());
    }

    public static boolean isPrimitive(Class cl) {
        return cl != null &&
                (cl.isPrimitive()
                        || isWrapperType(cl)
                        || String.class.equals(cl));
    }

    public static boolean isWrapperType(Class cl) {
        return WRAPPER_TYPES.contains(cl);
    }

    protected static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    public static boolean isArray(Object o) {
        return o != null && o.getClass().isArray();
    }

    public static boolean isPrimitiveArray(Object candidateArray) {
        if (candidateArray == null) {
            return false;
        }
        String type = candidateArray.getClass().getCanonicalName();
        return type != null && isArray(candidateArray) &&
                (type.equals("byte[]")
                        || type.equals("short[]")
                        || type.equals("int[]")
                        || type.equals("long[]")
                        || type.equals("float[]")
                        || type.equals("double[]")
                        || type.equals("boolean[]")
                        || type.equals("char[]")
                        || type.equals("java.lang.Byte[]")
                        || type.equals("java.lang.Short[]")
                        || type.equals("java.lang.Integer[]")
                        || type.equals("java.lang.Long[]")
                        || type.equals("java.lang.Float[]")
                        || type.equals("java.lang.Double[]")
                        || type.equals("java.lang.Boolean[]")
                        || type.equals("java.lang.Character[]"));
    }
}
