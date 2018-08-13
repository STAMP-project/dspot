package eu.stamp_project.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;

/**
 * User: Simon
 * Date: 23/03/16
 * Time: 15:48
 */
@SuppressWarnings("unchecked")
public class TypeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeUtils.class);

    private final static Set<Class<?>> WRAPPER_TYPES;

    static {
        WRAPPER_TYPES = new HashSet<>();
        WRAPPER_TYPES.add(Boolean.class);
        WRAPPER_TYPES.add(Character.class);
        WRAPPER_TYPES.add(Byte.class);
        WRAPPER_TYPES.add(Short.class);
        WRAPPER_TYPES.add(Integer.class);
        WRAPPER_TYPES.add(Long.class);
        WRAPPER_TYPES.add(Float.class);
        WRAPPER_TYPES.add(Double.class);
        WRAPPER_TYPES.add(Void.class);
    }

    public static boolean isPrimitiveCollection(Object object) {
        if (Collection.class.isInstance(object)) {
            Collection collection = (Collection) object;
            return collection.isEmpty() ||
                    collection.stream()
                            .anyMatch(TypeUtils::isPrimitive);
        }
        return false;
    }

    public static boolean isPrimitiveMap(Object object) {
        if(Map.class.isInstance(object)) {
            Map map = (Map) object;
            return map.isEmpty() ||
                    map.keySet()
                            .stream()
                            .anyMatch(TypeUtils::isPrimitive) &&
                            map.values()
                                    .stream()
                                    .anyMatch(TypeUtils::isPrimitive);
        }
        return false;
    }

    public static boolean isPrimitive(Object object) {
        return object != null &&
                isPrimitive(object.getClass());
    }

    public static boolean isPrimitive(Class cl) {
        return cl.isPrimitive()
                || isWrapperType(cl)
                || String.class.equals(cl);
    }

    private static boolean isWrapperType(Class cl) {
        return WRAPPER_TYPES.contains(cl);
    }

    public static boolean isArray(Object o) {
        return o != null && o.getClass().isArray();
    }

	public static boolean isPrimitive(CtTypeReference type) {
		try {
			return type.isPrimitive() || type.unbox().isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isString(CtTypeReference type) {
		try {
            return String.class.isAssignableFrom(type.getActualClass());
        } catch (spoon.support.SpoonClassNotFoundException ignored) {

		} catch (Exception e) {
		    e.printStackTrace();
            LOGGER.warn("Error during check isString on {}", type.toString());
		}
		return false;
	}
}
