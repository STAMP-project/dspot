package fr.inria.diversify.dspot.dynamic.logger;

import fr.inria.diversify.dspot.dynamic.logger.KeyWord;
import fr.inria.diversify.dspot.dynamic.logger.ObjectSerialiazer;

import java.util.*;

/**
 * User: Simon
 * Date: 23/03/16
 * Time: 13:21
 */
public class MethodCall {
    protected String method;
    protected Object[] parameters;
    protected char[] types;
    protected int deep;
    protected Object target;


    public MethodCall(String method, int deep, Object[] params) {
        this.method = method;
        this.deep = deep;

        parameters = new Object[params.length/2];
        types = new char[params.length/2];
        for(int i = 0; i < params.length/2; i++) {
            types[i] = (Character) params[(i*2) + 1];
            if(types[i] == KeyWord.objectType) {
                parameters[i] = new ObjectSerialiazer(params[i*2]);
            } else {
                parameters[i] = params[i*2];
            }
        }
    }

    public boolean sameMethod(String methodId, int deep) {
        return deep == this.deep && methodId.equals(method);
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < parameters.length; i++) {
            if(types[i] == KeyWord.primitiveType) {
                ret.append(KeyWord.methodCallPrimitiveParameter)
                        .append(KeyWord.simpleSeparator)
                        .append(primitiveToString(i));
            } else {
                ret.append(KeyWord.methodCallObjectParameter)
                        .append(KeyWord.simpleSeparator)
                        .append(parameters[i]);
            }
            ret.append(KeyWord.endLine);
        }
        ret.append(KeyWord.methodCallMethod).append(KeyWord.simpleSeparator).append(method).append(KeyWord.endLine);
        ret.append(KeyWord.methodCallReceiverType).append(KeyWord.simpleSeparator);
        if(target == null) {
            ret.append("static");
        } else {
            ret.append(target.getClass().getCanonicalName());
        }
        return ret.append(KeyWord.endLine).toString();
    }

    protected StringBuilder primitiveToString(int index) {
        StringBuilder builder = new StringBuilder();

        Object value = parameters[index];
        char type = types[index];
        if(type == KeyWord.collectionType) {
            builder.append(getCollectionType((Collection) value));
        } else if(type == KeyWord.mapType) {
            builder.append(getMapType((Map) value));
        } else {
            builder.append(value.getClass().getCanonicalName());
        }

        builder.append(KeyWord.simpleSeparator)
                .append(value);

        return builder;
    }

    protected String typeToString(int index) {
        Object value = parameters[index];
        char type = types[index];
        if(type == KeyWord.collectionType) {
            return getCollectionType((Collection) value);
        }
        if(type == KeyWord.mapType) {
            return getMapType((Map) value);
        }
        if(type == KeyWord.objectType) {
            return ((ObjectSerialiazer) value).toString();
        } else {
            return value.getClass().getCanonicalName();
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

    public List<Object> getObjectParameters() {
        List<Object> list = new LinkedList<Object>();
        for(int i = 0; i < parameters.length; i++) {
            if(types[i] == KeyWord.objectType) {
                list.add(((ObjectSerialiazer)parameters[i]).getObject());
            }
        }
        return list;
    }
}
