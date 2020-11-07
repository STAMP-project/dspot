package eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs;


import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.FailToObserveException;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.MethodsHandler;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.Observation;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ObjectLogUtils;
import eu.stamp_project.testrunner.EntryPoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
    private int maxDeep = 3;

    private ObjectLog() {
        this.observations = new LinkedHashMap<>();
        this.methodsHandler = new MethodsHandler();
    }

    private static ObjectLog getSingleton() {
        if (singleton == null) {
            singleton = new ObjectLog();
        }
        return singleton;
    }

    public static synchronized void reset() {
        singleton = new ObjectLog();
    }

    public static synchronized void log(Object objectToObserve, String objectObservedAsString, String id) {
        getSingleton()._log(
                objectToObserve,
                objectToObserve,
                null,
                objectObservedAsString,
                id,
                0,
                new ArrayList<>()
        );
    }

    private synchronized void _log(Object startingObject,
                      Object objectToObserve,
                      Class<?> currentObservedClass,
                      String observedObjectAsString,
                      String id,
                      int deep,
                      List<Method> methodsToReachCurrentObject) {
        if (deep <= maxDeep) {
            final boolean primitive = ObjectLogUtils.isPrimitive(objectToObserve);
            final boolean primitiveArray = ObjectLogUtils.isPrimitiveArray(objectToObserve);
            final boolean primitiveCollectionOrMap = ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(objectToObserve);
            if (objectToObserve == null) {
                addObservation(id, observedObjectAsString, null);
            } else if (isSerializable(objectToObserve) &&
                    (primitive || primitiveArray || primitiveCollectionOrMap)) {
                addObservation(id, observedObjectAsString, objectToObserve);
            } else if (ObjectLogUtils.isCollection(objectToObserve)) { // the object is empty here
                addObservation(id, observedObjectAsString + ".isEmpty()", ((Collection) objectToObserve).isEmpty());
            } else if (ObjectLogUtils.isMap(objectToObserve)) {
                addObservation(id, observedObjectAsString + ".isEmpty()", ((Map) objectToObserve).isEmpty());
            } else if (!objectToObserve.getClass().getName().toLowerCase().contains("mock")) {
                if(objectToObserve.getClass().isArray()) {
                    ArrayList<Integer> dimensions = createDimensionList(startingObject);
                    goThroughArray(startingObject,
                            observedObjectAsString,
                            id,
                            deep,
                            methodsToReachCurrentObject,
                            0,
                            dimensions);
                } else {
                    observeNotNullObject(
                            startingObject,
                            currentObservedClass == null ? objectToObserve.getClass() : currentObservedClass,
                            observedObjectAsString,
                            id,
                            deep,
                            methodsToReachCurrentObject,
                            false,
                            new ArrayList<>());
                }
            }
        }
    }

    public static boolean isSerializable(Object candidate) {
        try {
            new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(candidate);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private synchronized void addObservation(String id, String observedObjectAsString, Object actualValue) {
        if (isSerializable(actualValue)) {
            if (actualValue instanceof String &&
                    // we forbid absolute paths
                    // we allow relative paths
                    // but it can be error-prone
                    // watch out
                    new File((String)actualValue).isAbsolute()) {
                return;
            }
            if (!observations.containsKey(id)) {
                observations.put(id, new Observation());
            }
            observations.get(id).add(observedObjectAsString, actualValue);
        }
    }

    private synchronized Object chainInvocationOfMethods(List<Method> methodsToInvoke, Object startingObject) throws FailToObserveException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask task = null;
        Object currentObject = null;
        try {
            try {
                task = new FutureTask<>(() -> methodsToInvoke.get(0).invoke(startingObject));
                executor.execute(task);
                currentObject = task.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new FailToObserveException();
            }
            for (int i = 1; i < methodsToInvoke.size(); i++) {
                Method method = methodsToInvoke.get(i);
                try {
                    final Object finalCurrentObject = currentObject;
                    task = new FutureTask<>(() -> method.invoke(finalCurrentObject));
                    executor.execute(task);
                    currentObject = task.get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new FailToObserveException();
                }
            }
        } finally {
            if (task != null) {
                task.cancel(true);
            }
            executor.shutdown();
        }
        return currentObject;
    }

    private ArrayList<Integer> createDimensionList(Object startingObject) {
        ArrayList<Integer> dimensionList = new ArrayList<>();
        int dimensions = 1 + startingObject.getClass().getName().lastIndexOf('[');
        for (int i = 0; i < dimensions; i++) {
            dimensionList.add(0);
        }
        return dimensionList;
    }

    private void goThroughArray(Object object,
                                String stringObject,
                                String id,
                                int deep,
                                List<Method> methodsToReachCurrentObject,
                                int depth,
                                ArrayList<Integer> dimensions) {
        int size = Array.getLength(object);
        for (int i = 0; i < size; i++) {
            dimensions.set(depth,i);
            Object value = Array.get(object, i);
            if(value == null){
                String observedArrayAsString = buildArrayType(null,stringObject,dimensions,false);
                addObservation(id, observedArrayAsString, null);
            } else if (value.getClass().isArray()) {
                goThroughArray(value,
                        stringObject,
                        id,
                        deep,
                        methodsToReachCurrentObject,
                        (depth+1),
                        dimensions);
            } else {
                observeNotNullObject(value,
                        value.getClass(),
                        stringObject,
                        id,
                        deep,
                        methodsToReachCurrentObject,
                        true,
                        dimensions);
            }
        }
    }

    private String buildArrayType(Class currentObservedClass,
                                  String stringObject,
                                  ArrayList<Integer> dimensions,
                                  boolean shouldCast){
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for(int j = 0; j<dimensions.size();j++){
            sb1.append("[" + dimensions.get(j) + "]");
            sb2.append("[]");
        }
        String observedArrayAsString;
        if(shouldCast){
            String nameOfVisibleClass = getVisibleClass(currentObservedClass);
            nameOfVisibleClass = nameOfVisibleClass.substring(0, (nameOfVisibleClass.length() - 1)) + sb2.toString() + ")";
            observedArrayAsString = "(" + nameOfVisibleClass + stringObject + ")" + sb1.toString();
        } else {
            observedArrayAsString = "(" + stringObject + ")" + sb1.toString();
        }
        return observedArrayAsString;
    }

    private void observeNotNullObject(Object startingObject,
                                      Class<?> currentObservedClass,
                                      String stringObject,
                                      String id,
                                      int deep,
                                      List<Method> methodsToReachCurrentObject,
                                      boolean isArrayComponent,
                                      ArrayList<Integer> dimensions) {
        try {
            for (Method method : methodsHandler.getAllMethods(currentObservedClass)) {
                try {
                    final ArrayList<Method> tmpListOfMethodsToReachCurrentObject = new ArrayList<>(methodsToReachCurrentObject);
                    tmpListOfMethodsToReachCurrentObject.add(method);
                    final Object result = chainInvocationOfMethods(tmpListOfMethodsToReachCurrentObject, startingObject);
                    String observedObjectAsString = getObservedObjectAsString(startingObject,
                            currentObservedClass,
                            stringObject,
                            method,
                            isArrayComponent,
                            dimensions);
                    _log(startingObject,
                            result,
                            method.getReturnType(),
                            observedObjectAsString ,
                            id,
                            deep + 1,
                            tmpListOfMethodsToReachCurrentObject
                    );
                    tmpListOfMethodsToReachCurrentObject.remove(method);
                } catch (FailToObserveException ignored) {
                    // ignored, we just do nothing...
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getObservedObjectAsString(Object startingObject,
                                             Class<?> currentObservedClass,
                                             String stringObject,
                                             Method method,
                                             boolean isArrayComponent,
                                             ArrayList<Integer> dimensions){
        String observedObjectAsString;
        if(isArrayComponent){
            String observedArrayAsString = buildArrayType(currentObservedClass,
                    stringObject,
                    dimensions,
                    !currentObservedClass.isAnonymousClass());
            observedObjectAsString = observedArrayAsString + "." + method.getName() + "()";
        } else {
            if (startingObject.getClass().isAnonymousClass()) {
                observedObjectAsString = "(" + stringObject + ")." + method.getName() + "()";
            } else {
                String nameOfVisibleClass = getVisibleClass(currentObservedClass);
                observedObjectAsString = "(" + nameOfVisibleClass + stringObject + ")." + method.getName() + "()";
            }
        }
        return observedObjectAsString;
    }

    private synchronized String getVisibleClass(Class<?> currentObservedClass) {
        if (currentObservedClass == null || currentObservedClass == Object.class) {
            return "";
        } else if (Modifier.isPrivate(currentObservedClass.getModifiers()) ||
                Modifier.isProtected(currentObservedClass.getModifiers())) {
            return getVisibleClass(currentObservedClass.getSuperclass());
        } else {
            return "(" + currentObservedClass.getCanonicalName() + ")";
        }
    }

    public synchronized static Map<String, Observation> getObservations() {
        if (getSingleton().observations.isEmpty()) {
            return load();
        } else {
            return getSingleton().observations;
        }
    }

    private static final String OBSERVATIONS_PATH_FILE_NAME = "target/dspot/observations.ser";

    public synchronized static void save() {
        final File file = new File(OBSERVATIONS_PATH_FILE_NAME);
        file.getParentFile().mkdirs();
        getSingleton().observations.values().forEach(Observation::purify);
        try (FileOutputStream fout = new FileOutputStream(OBSERVATIONS_PATH_FILE_NAME)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                oos.writeObject(getSingleton().observations);
                System.out.println(
                        String.format("File saved to the following path: %s",
                                file.getAbsolutePath())
                );
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public synchronized static Map<String, Observation> load() {
        try (FileInputStream fi = new FileInputStream(new File(
                (EntryPoint.workingDirectory != null ? // in case we modified the working directory
                        EntryPoint.workingDirectory.getAbsolutePath() + "/" : "") +
                        OBSERVATIONS_PATH_FILE_NAME))) {
            try (ObjectInputStream oi = new ObjectInputStream(fi)) {
                return (Map) oi.readObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
