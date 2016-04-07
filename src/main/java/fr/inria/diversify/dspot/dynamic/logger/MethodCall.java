package fr.inria.diversify.dspot.dynamic.logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * User: Simon
 * Date: 23/03/16
 * Time: 13:21
 */
public class MethodCall {
    protected String method;
    protected Object[] parameters;
    protected int deep;
    protected Object target;

    public MethodCall(String method, int deep, Object[] parameters) {
        this.method = method;
        this.parameters = parameters;
        this.deep = deep;
    }

    public boolean sameMethod(String methodId, int deep) {
        return deep == this.deep && methodId.equals(method);
    }

    public String toString() {
        String ret = "";
        for(Object param : parameters) {
            ret +=  typeToString(param);
            ret += ":" + param + "\n";
        }
         ret += method + "\ndynamicType:";
        if(target == null) {
            ret += "static";
        } else {
            ret += target.getClass().getCanonicalName();
        }
        return ret;
    }

    protected String typeToString(Object object) {
        if(Collection.class.isInstance(object)) {
            return getCollectionType((Collection)object);
        }
        if(Map.class.isInstance(object)) {
            return getMapType((Map)object);
        } else {
            return object.getClass().getCanonicalName();
        }
    }

    protected String getCollectionType(Collection collection) {
        if(!collection.isEmpty()) {
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next != null) {
                    return collection.getClass().getCanonicalName() + "<" + next.getClass().getCanonicalName() + ">";
                }
            }
        }
        return collection.getClass().getCanonicalName() + "<null>";
    }

    protected  String getMapType(Map map) {
        String keyType = "null";
        String valueType = "null";
        Iterator keyIterator = map.keySet().iterator();
        while (keyIterator.hasNext()) {
            Object next = keyIterator.next();
            if(next != null) {
                keyType = next.getClass().getCanonicalName();
                break;
            }
        }
        Iterator valueIterator = map.keySet().iterator();
        while (valueIterator.hasNext()) {
            Object next = valueIterator.next();
            if (next != null) {
                valueType = next.getClass().getCanonicalName();
                break;
            }
        }
        return map.getClass().getCanonicalName() + "<" + keyType + ", " + valueType + ">";
    }

    public int hashCode() {
        return method.hashCode() * 31  + Arrays.hashCode(parameters);
    }

    public void setTarget(Object target) {
        this.target = target;
    }
}
