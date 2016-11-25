package fr.inria.diversify.compare;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import fr.inria.diversify.dspot.dynamic.logger.TypeUtils;

/**
 * User: Simon
 * Date: 23/10/15
 * Time: 14:31
 */
public class ObjectLog {
    private static ObjectLog singleton ;
    protected Map<String, Observation> observations;
    protected MethodsHandler methodsHandler;
    protected Invocator invocator;
    protected int maxDeep = 4;
    protected Map<String, Object> objects;

    private ObjectLog() {
        this.observations = new HashMap<String, Observation>();
        this.objects = new HashMap<String, Object>();
        this.methodsHandler = new MethodsHandler(true, true);
        this.invocator = new Invocator(1);
    }

    protected static ObjectLog getSingleton() {
        if(singleton == null) {
            singleton = new ObjectLog();
        }
        return singleton;
    }

    public static void reset() {
        singleton = new ObjectLog();
    }

    public static void log(Object object, String stringObject, String positionId) {
        logObject(object, stringObject, positionId);
        getSingleton().pLog(object, stringObject, positionId, 0);
    }

    public static void logObject(Object object, String stringObject, String positionId) {
        if(object != null) {
            getSingleton().objects.put(stringObject, object);
        }
    }

    public void pLog(Object object, String stringObject, String positionId, int deep) {
        if(deep < maxDeep) {
            if (object == null) {
                addObservation(positionId, stringObject, null);
            } else if (TypeUtils.isPrimitive(object)) {
                addObservation(positionId, stringObject, object);
            } else if (TypeUtils.isPrimitiveArray(object)) {
                addObservation(positionId, stringObject, object);
            } else if(TypeUtils.isPrimitiveCollectionOrMap(object)) {
//                typeOfIterable(object);
                addObservation(positionId, stringObject, object);
            } else {
                compareWithPreviousObjects(object, stringObject, positionId);
                observeNotNullObject(object, stringObject, positionId, deep);
            }
        }
    }

    protected void addObservation(String positionId, String stringObject, Object value) {
        if(!observations.containsKey(positionId)) {
            observations.put(positionId, new Observation());
        }
        observations.get(positionId).add(stringObject, value);
    }

    protected void compareWithPreviousObjects(Object object, String stringObject, String positionId) {
        try {
            for (String key : objects.keySet()) {
                if (!key.equals(stringObject)) {
                    if (objects.get(key).equals(object)) {
                        addObservation(positionId, stringObject + ".equals(" + key + ")", true);
                    }
                }
            }
        } catch (Exception e) {}
    }

    protected void observeNotNullObject(Object o,  String stringObject, String positionId, int deep) {
        if(deep < maxDeep) {
            for (Method method : methodsHandler.getAllMethods(o)) {
                Invocation invocation = new Invocation(o, method);
                invocator.invoke(invocation);

                if (invocation.getError() == null) {
                    String castType = o.getClass().getCanonicalName();
                    pLog(invocation.getResult(), "((" + castType + ")"
                            + stringObject + ")." + method.getName() + "()", positionId, deep + 1);
                }
            }
        }
    }

    public static Map<String, Observation> getObservations() {
        return singleton.observations;
    }
}
