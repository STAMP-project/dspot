package fr.inria.diversify.compare;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


public class MethodsHandler {

    private Map<Class<?>, List<Method>> cache;
    private static final List<String> forbiddenMethods;

    static {
        forbiddenMethods = new ArrayList<>();
        forbiddenMethods.add("equals");
        forbiddenMethods.add("hashCode");
        forbiddenMethods.add("notify");
        forbiddenMethods.add("notifyAll");
        forbiddenMethods.add("wait");
        forbiddenMethods.add("getClass");
        forbiddenMethods.add("toString");
        forbiddenMethods.add("display");
        forbiddenMethods.add("clone");
        forbiddenMethods.add("hasExtensions");

        // since we generate contains(), we don't need to observe iterators
        forbiddenMethods.add("iterator");
        forbiddenMethods.add("spliterator");
        forbiddenMethods.add("listIterator");
        forbiddenMethods.add("stream");
        forbiddenMethods.add("parallelStream");
    }

    public MethodsHandler() {
        this.cache = new HashMap<>();
    }

    public List<Method> getAllMethods(Class<?> clazz) {
        if (!cache.containsKey(clazz)) {
            findMethods(clazz);
        }
        return cache.get(clazz);
    }

    private void findMethods(Class<?> clazz) {
        List<Method> methodsList = new ArrayList<Method>();
        for (Method m : clazz.getMethods()) {
            if (!forbiddenMethods.contains(m.getName()) && isValidMethod(m)) {
                methodsList.add(m);
            }
        }
        cache.put(clazz, methodsList);
    }

    private boolean isValidMethod(Method m) {
        if (!Modifier.isPublic(m.getModifiers())
                || Modifier.isStatic(m.getModifiers())
                || isVoid(m.getReturnType())
                || m.getReturnType() == Class.class) {
            return false;
        }
        Class<?>[] parameterTypes = m.getParameterTypes();
        return parameterTypes.length == 0; // we only consider methods that take no parameter
    }

    private static boolean isVoid(Class<?> type) {
        return type.equals(Void.class) || type.equals(void.class);
    }

}
