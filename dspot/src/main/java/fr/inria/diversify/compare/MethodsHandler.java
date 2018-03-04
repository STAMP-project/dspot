package fr.inria.diversify.compare;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


public class MethodsHandler {

    private Map<Class<?>, List<Method>> cache;
    private static final List<String> ignoredMethods;

    static {
        ignoredMethods = new ArrayList<>();
        ignoredMethods.add("equals");
        ignoredMethods.add("hashCode");
        ignoredMethods.add("notify");
        ignoredMethods.add("notifyAll");
        ignoredMethods.add("wait");
        ignoredMethods.add("getClass");
        ignoredMethods.add("toString");
        ignoredMethods.add("display");
        ignoredMethods.add("clone");
        ignoredMethods.add("hasExtensions");
    }

    public MethodsHandler() {
        this.cache = new HashMap<>();
    }

    public List<Method> getAllMethods(Object o) {
        if (!cache.containsKey(o.getClass())) {
            findMethods(o);
        }
        return cache.get(o.getClass());
    }

    private void findMethods(Object o) {
        List<Method> methodsList = new ArrayList<Method>();
        for (Method m : o.getClass().getMethods()) {
            if (!ignoredMethods.contains(m.getName()) && isValidMethod(m)) {
                methodsList.add(m);
            }
        }
        cache.put(o.getClass(), methodsList);
    }

    private boolean isValidMethod(Method m) {
        if (!Modifier.isPublic(m.getModifiers())
                || Modifier.isStatic(m.getModifiers())
                || isVoid(m.getReturnType())) {
            return false;
        }
        Class<?>[] parameterTypes = m.getParameterTypes();
        return parameterTypes.length == 0; // we only consider methods that take no parameter
    }

    private static boolean isVoid(Class<?> type) {
        return type.equals(Void.class) || type.equals(void.class);
    }

}
