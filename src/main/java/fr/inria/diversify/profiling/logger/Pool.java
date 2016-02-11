package fr.inria.diversify.profiling.logger;

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
        String canon = StringPool.putIfAbsent(str, str);
        return (canon == null) ? str : canon;
    }

    public static Integer get(Integer str) {
        Integer canon = IntegerPool.putIfAbsent(str, str);
        return (canon == null) ? str : canon;
    }
    public static Object get(Object str) {
        Object canon = ObjectPool.putIfAbsent(str, str);
        return (canon == null) ? str : canon;
    }

    public static void reset() {
        StringPool.clear();
        IntegerPool.clear();
        ObjectPool.clear();
    }
}

