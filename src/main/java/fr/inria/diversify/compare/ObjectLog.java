package fr.inria.diversify.compare;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Simon
 * Date: 23/10/15
 * Time: 14:31
 */
public class ObjectLog {
    private static ObjectLog singleton ;
    protected Map<Integer, Observation> observations;
    protected MethodsHandler methodsHandler;
    protected Invocator invocator;
    protected int maxDeep = 2;


    private ObjectLog() {
        this.observations = new HashMap<Integer, Observation>();
        methodsHandler = new MethodsHandler(true, true);
        invocator = new Invocator(1);
    }

    public static void reset() {
        singleton = new ObjectLog();
    }

    public static void log(Object object, String stringObject, int positionId) {
        if(singleton == null) {
            singleton = new ObjectLog();
        }
        singleton.pLog(object, stringObject, positionId, 0);
    }

    public void pLog(Object object, String stringObject, int positionId, int deep) {
        if(deep < maxDeep) {
            if (object == null) {
                addObservation(positionId, stringObject, null);
            } else if (isPrimitive(object)) {
                addObservation(positionId, stringObject, object);
            } else if (isPrimitiveArray(object)) {
                addObservation(positionId, stringObject, object);
            } else {
                observeNotNullObject(object, stringObject, positionId, deep);
            }
        }
    }

    protected void addObservation(int positionId, String stringObject, Object value) {
        if(!observations.containsKey(positionId)) {
            observations.put(positionId, new Observation());
        }
        observations.get(positionId).add(stringObject, value);
    }



    protected void observeNotNullObject(Object o,  String stringObject, int positionId, int deep) {
        for(Method method :methodsHandler.getAllMethods(o)) {
            Invocation invocation = new Invocation(o, method);
            invocator.invoke(invocation);

            if(invocation.getError() == null) {
                String castType = o.getClass().getCanonicalName();
                pLog(invocation.getResult(),"((" + castType + ")"
                        + stringObject + ")." + method.getName() + "()", positionId, deep + 1);
            }
        }
    }

    protected boolean isPrimitive(Object o) {
        String type = o.getClass().getCanonicalName();

        return o != null &&
                (o.getClass().isPrimitive()
                || type.equals("java.lang.Byte")
                || type.equals("java.lang.Short")
                || type.equals("java.lang.Integer")
                || type.equals("java.lang.Long")
                || type.equals("java.lang.Float")
                || type.equals("java.lang.Double")
                || type.equals("java.lang.Boolean")
                || type.equals("java.lang.Character"));
    }

    protected boolean isArray(Object o) {
        return o != null && o.getClass().isArray();
    }

    protected boolean isPrimitiveArray(Object o) {
        String type = o.getClass().getCanonicalName();
        return type != null && isArray(o) &&
                (type.equals("byte[]")
                        || type.equals("short[]")
                        || type.equals("int[]")
                        || type.equals("long[]")
                        || type.equals("float[]")
                        || type.equals("double[]")
                        || type.equals("boolean[]")
                        || type.equals("char[]"));
    }


    public static Map<Integer, Observation> getObservations() {
        return singleton.observations;
    }
}
