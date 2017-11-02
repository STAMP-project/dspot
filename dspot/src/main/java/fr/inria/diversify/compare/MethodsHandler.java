package fr.inria.diversify.compare;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


public class MethodsHandler {

    private Map<Class<?>, List<Method>> cache;
    private static final Map<String, Class> ignoredMethods;

    static {
        Class cl = Object.class;
        ignoredMethods = new HashMap<>();
        ignoredMethods.put("equals", cl);
        ignoredMethods.put("hashCode", cl);
        ignoredMethods.put("notify", cl);
        ignoredMethods.put("notifyAll", cl);
        ignoredMethods.put("wait", cl);
        ignoredMethods.put("getClass", cl);
        ignoredMethods.put("toString", cl);
        ignoredMethods.put("display", cl);

        ignoredMethods.put("clone", cl);

        ignoredMethods.put("hasExtensions", cl);
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
            if (ignoredMethods.get(m.getName()) == null && isValidMethod(m)) {
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
        return parameterTypes.length == 0; // we only consider tests that take no parameters
    }

    private static boolean isVoid(Class<?> type) {
        return type.equals(Void.class) || type.equals(void.class);
    }

}
