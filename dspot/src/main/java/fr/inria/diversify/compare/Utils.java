package fr.inria.diversify.compare;

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
            return isPrimitiveCollection(collectionOrMap) || isPrimitiveMap(collectionOrMap);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPrimitiveCollection(Object object) {
        if (Collection.class.isInstance(object)) {
            Collection collection = (Collection) object;
            if (collection.isEmpty()) {
                return true;
            } else {
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    if (next != null) {
                        return isPrimitive(next);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isPrimitiveMap(Object object) {
        if (Map.class.isInstance(object)) {
            Map map = (Map) object;
            if (map.isEmpty()) {
                return true;
            } else {
                boolean isKeyPrimitive = false;
                boolean isValuePrimitive = false;
                Iterator keyIterator = map.keySet().iterator();
                while (keyIterator.hasNext()) {
                    Object next = keyIterator.next();
                    if (next != null && isPrimitive(next)) {
                        isKeyPrimitive = true;
                        break;
                    }
                }
                if (isKeyPrimitive) {
                    Iterator valueIterator = map.keySet().iterator();
                    while (valueIterator.hasNext()) {
                        Object next = valueIterator.next();
                        if (next != null && isPrimitive(next)) {
                            isValuePrimitive = true;
                            break;
                        }
                    }
                }
                return isKeyPrimitive && isValuePrimitive;
            }
        }
        return false;
    }

    public static boolean isPrimitive(Object object) {
        return isPrimitive(object.getClass());
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

    public static boolean isPrimitiveArray(Object o) {
        String type = o.getClass().getCanonicalName();
        return type != null && isArray(o) &&
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
