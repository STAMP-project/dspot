package fr.inria.diversify.dspot.dynamic.logger;

import java.util.*;

/**
 * User: Simon
 * Date: 30/09/16
 * Time: 14:32
 */
public class Env {
    protected IdentityHashMap<Object, Set<String>> fieldsByObject;
    protected List<Env> subEnvironments;

    public void add(Object object, String fieldId) {
        if(fieldsByObject == null) {
            fieldsByObject = new IdentityHashMap<Object, Set<String>>();
        }
        Set<String> set = fieldsByObject.get(object);
        if(set == null) {
            set = new HashSet<String>();
            fieldsByObject.put(object, set);
        }
        set.add(fieldId);
    }

    public boolean contains(Object object, String fieldId) {
        if(fieldsByObject != null) {
            Set<String> set = fieldsByObject.get(object);
            if(set != null && set.contains(fieldId)) {
                return true;
           } else {
                if (subEnvironments != null) {
                    for (Env env : subEnvironments) {
                        if (env.contains(object, fieldId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void addSubEnv(Env subEnv) {
        if(subEnvironments == null) {
            subEnvironments = new LinkedList<Env>();
        }
        subEnvironments.add(subEnv);
    }
}
