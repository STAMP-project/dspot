package fr.inria.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/03/18.
 */
public class ClassWithMap {

    private Map<Object, Object> emptyMap = Collections.emptyMap();

    private Map<String, String> fullMap;

    public ClassWithMap(){
        this.fullMap = new HashMap<>();
        this.fullMap.put("key1", "value1");
        this.fullMap.put("key2", "value2");
        this.fullMap.put("key3", "value3");
    }

    public Map<Object, Object> getEmptyMap() {
        return emptyMap;
    }

    public Map<String, String> getFullMap() {
        return fullMap;
    }
}
