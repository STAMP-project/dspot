package fr.inria.diversify.dspot.dynamic.logger;

import java.util.*;

/**
 * User: Simon
 * Date: 06/09/16
 * Time: 11:41
 */
public class PrimitiveCache {
    Map<Integer, Map<Integer, Set<String>>> cache;
    static int maxSize = 20;

    public PrimitiveCache() {
        cache = new HashMap<Integer, Map<Integer, Set<String>>>();
    }

    public boolean alreadyLog(int constructorId, int argIndex, Object value) {
        Set<String> argCache = getCacheFor(constructorId, argIndex);
        String valueString = value + "";
        if(argCache.size() > maxSize || argCache.contains(valueString)) {
            return true;
        } else {
            argCache.add(valueString);
            return false;
        }
    }

    private Set<String> getCacheFor(int constructorId, int argIndex) {
        if(!cache.containsKey(constructorId)) {
            cache.put(constructorId, new HashMap<Integer, Set<String>>());
        }
        Map<Integer, Set<String>> constructorCache = cache.get(constructorId);

        if(!constructorCache.containsKey(argIndex)) {
            constructorCache.put(argIndex, new HashSet<String>());
        }

        return constructorCache.get(argIndex);
    }
}
