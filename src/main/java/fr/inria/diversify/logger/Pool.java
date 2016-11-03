package fr.inria.diversify.logger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Simon
 * Date: 05/06/15
 * Time: 12:59
 */
public class Pool {
    private static Map<String,String> StringPool =
            new HashMap<String,String>(20000);

    private static Map<Integer,Integer> IntegerPool =
            new HashMap<Integer,Integer>(20000);

    private static Map<Object,Object> ObjectPool =
            new HashMap<Object,Object>(20000);

    public static String get(String str) {
        String v = StringPool.get(str);
        if (v == null) {
            v = StringPool.put(str, str);
        }
        return (v == null) ? str : v;
    }

    public static Integer get(Integer str) {
        Integer v = IntegerPool.get(str);
        if (v == null) {
            v = IntegerPool.put(str, str);
        }
        return (v == null) ? str : v;
    }
    public static Object get(Object str) {
        Object v = ObjectPool.get(str);
        if (v == null) {
            v = ObjectPool.put(str, str);
        }
        return (v == null) ? str : v;
    }

    public static void reset() {
        StringPool.clear();
        IntegerPool.clear();
        ObjectPool.clear();
    }
}

