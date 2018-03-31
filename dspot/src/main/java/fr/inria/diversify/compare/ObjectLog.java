package fr.inria.diversify.compare;


import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * User: Simon
 * Date: 23/10/15
 * Time: 14:31
 */
public class ObjectLog {
    private static ObjectLog singleton;
    private Map<String, Observation> observations;
    private MethodsHandler methodsHandler;
    private int maxDeep = 4;

    private ObjectLog() {
        this.observations = new HashMap<>();
        this.methodsHandler = new MethodsHandler();
    }

    private static ObjectLog getSingleton() {
        if (singleton == null) {
            singleton = new ObjectLog();
        }
        return singleton;
    }

    public static void reset() {
        singleton = new ObjectLog();
    }

    public static void log(Object object, String stringObject, String positionId) {
        getSingleton()._log(object, object.getClass(), stringObject, positionId, 0);
    }

    private void _log(Object object, Class<?> clazz, String stringObject, String positionId, int deep) {
        if (deep < maxDeep) {
            if (object == null) {
                addObservation(positionId, stringObject, null);
            } else if (Utils.isPrimitive(object)) {
                addObservation(positionId, stringObject, object);
            } else if (Utils.isPrimitiveArray(object)) {
                addObservation(positionId, stringObject, object);
            } else if (Utils.isPrimitiveCollectionOrMap(object)) {
                addObservation(positionId, stringObject, object);
            } else {
                observeNotNullObject(object, clazz, stringObject, positionId, deep);
            }
        }
    }

    private void addObservation(String positionId, String stringObject, Object value) {
        if (!observations.containsKey(positionId)) {
            observations.put(positionId, new Observation());
        }
        observations.get(positionId).add(stringObject, value);
    }

    private void observeNotNullObject(Object o,
                                      Class<?> clazz,
                                      String stringObject,
                                      String positionId,
                                      int deep) {
        if (deep < maxDeep && !o.getClass().getName().toLowerCase().contains("mock")) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            FutureTask task = null;
            try {
                for (Method method : methodsHandler.getAllMethods(clazz)) {
                    try {
                        task = new FutureTask<>(() -> method.invoke(o));
                        executor.execute(task);
                        final Object result = task.get(1, TimeUnit.SECONDS);
                        if (o.getClass().isAnonymousClass()) {
                            _log(result,
                                    method.getReturnType(),
                                    "(" + stringObject + ")." + method.getName() + "()",
                                    positionId, deep + 1
                            );
                        } else {
                            _log(result,
                                    method.getReturnType(),
                                    "((" + clazz.getCanonicalName() + ")" + stringObject + ")." + method.getName() + "()",
                                    positionId, deep + 1
                            );
                        }
                    } catch (Exception ignored) {
                        // ignored, we skip this iteration and continue;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (task != null) {
                    task.cancel(true);
                }
                executor.shutdown();
            }
        }
    }

    public static Map<String, Observation> getObservations() {
        return singleton.observations;
    }

}
