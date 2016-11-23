package fr.inria.diversify.dspot.dynamic.logger;


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
            if(types[i] == KeyWord.objectType && params[i*2] != null) {
                parameters[i] = new ObjectSerialiazer(params[i*2]);
            } else {
                parameters[i] = params[i * 2];
            }
        }
    }

    public boolean sameMethod(String methodId, int deep) {
        return deep == this.deep && methodId.equals(method);
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < parameters.length; i++) {
            char type = types[i];
            if(type == KeyWord.objectType ) {
                ret.append(KeyWord.methodCallObjectParameter)
                        .append(KeyWord.simpleSeparator)
                        .append(parameters[i]);
            } else {
                ret.append(KeyWord.methodCallPrimitiveParameter)
                        .append(KeyWord.simpleSeparator)
                        .append(primitiveToString(i));
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
            if(value != null) {
                builder.append(value.getClass().getCanonicalName());
            } else {
                builder.append("null");
            }
        }
        if(value != null && value.getClass().isArray()) {
            builder.append(KeyWord.simpleSeparator)
                    .append(arrayToString(value));
        } else {
            builder.append(KeyWord.simpleSeparator)
                    .append(value);
        }

        return builder;
    }

    private String arrayToString(Object value) {
        String cl = value.getClass().toString();
        if(cl.endsWith("[C")) {
            return Arrays.toString((char[]) value);
        }
        if(cl.endsWith("[I")) {
            return Arrays.toString((int[]) value);
        }
        if(cl.endsWith("[J")) {
            return Arrays.toString((long[]) value);
        }
        if(cl.endsWith("[D")) {
            return Arrays.toString((double[]) value);
        }
        if(cl.endsWith("[F")) {
            return Arrays.toString((float[]) value);
        }
        if(cl.endsWith("[B")) {
            return Arrays.toString((byte[]) value);
        }
        if(cl.endsWith("[Z")) {
            return Arrays.toString((boolean[]) value);
        }
        return Arrays.toString((Object[]) value);
    }

    protected String getCollectionType(Collection collection) {
        if(collection != null) {
            if (!collection.isEmpty()) {
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
        return ArrayList.class.getCanonicalName() + "<null>";
    }

    protected  String getMapType(Map map) {
         if(map != null) {
             String keyType = "null";
             String valueType = "null";
             Iterator keyIterator = map.keySet().iterator();
             while (keyIterator.hasNext()) {
                 Object next = keyIterator.next();
                 if (next != null) {
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
        return HashMap.class.getCanonicalName() + "< null, null>";
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

    public String getMethod() {
        return method;
    }
}
