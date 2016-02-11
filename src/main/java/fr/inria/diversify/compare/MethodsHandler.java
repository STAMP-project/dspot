package fr.inria.diversify.compare;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


public class MethodsHandler {

    protected Map<Class<?>, List<Method>> cache;
    protected Random random;
    protected static final Map<String, Class> ignoredMethods;
    protected boolean onlyPublicMethod;
    protected boolean onlyNotStaticMethod;

    static {
        Class cl = Object.class;
        ignoredMethods = new HashMap<String, Class>();
        ignoredMethods.put("equals", cl);
        ignoredMethods.put("hashCode", cl);
        ignoredMethods.put("notify", cl);
        ignoredMethods.put("notifyAll", cl);
        ignoredMethods.put("wait", cl);
        ignoredMethods.put("getClass", cl);
        ignoredMethods.put("toString", cl);
        ignoredMethods.put("display", cl);

        ignoredMethods.put("hasExtensions",cl);
    }

    public MethodsHandler(Random random, boolean onlyPublicMethod, boolean onlyNotStaticMethod) {
        this.cache = new HashMap<Class<?>, List<Method>>();
        this.random = random;
        this.onlyNotStaticMethod = onlyNotStaticMethod;
        this.onlyPublicMethod = onlyPublicMethod;
    }

    public MethodsHandler(boolean onlyPublicMethod, boolean onlyNotStaticMethod) {
        this.cache = new HashMap<Class<?>, List<Method>>();
        this.random = new Random();
        this.onlyNotStaticMethod = onlyNotStaticMethod;
        this.onlyPublicMethod = onlyPublicMethod;
    }

    public List<Method> getRandomMethods(Object o, int nbMethod) {
        if (!cache.containsKey(o.getClass())) {
            findMethods(o);
        }

        List<Method> objectMethods = new LinkedList<Method>(cache.get(o.getClass()));
        List<Method> randomMethods = new ArrayList<Method>(nbMethod);

        while (randomMethods.size() < nbMethod && !objectMethods.isEmpty()) {
            int randomIndex = random.nextInt(objectMethods.size());
            Method m = objectMethods.remove(randomIndex);
            m.setAccessible(true);
            randomMethods.add(m);
        }
        return randomMethods;

    }

    public List<Method> getAllMethods(Object o) {
        if (!cache.containsKey(o.getClass())) {
            findMethods(o);
        }
        return cache.get(o.getClass());
    }

    protected void findMethods(Object o) {
        List<Method> methodsList = new ArrayList<Method>();
        for (Method m : o.getClass().getMethods()) {
            if (ignoredMethods.get(m.getName()) == null && isValidMethod(m)) {
                methodsList.add(m);
            }
        }
        cache.put(o.getClass(), methodsList);
    }

    protected boolean isValidMethod(Method m) {
        if ((onlyPublicMethod && !Modifier.isPublic(m.getModifiers()))
                || (onlyNotStaticMethod && Modifier.isStatic(m.getModifiers()))
                || isVoid(m.getReturnType())) {
            return false;
        }
        Class<?>[] parameterTypes = m.getParameterTypes();
        if (parameterTypes.length != 0) { // we only consider tests that take no parameters
            return false;
        }
        return true;
    }

    protected boolean isVoid(Class<?> type) {
        return type.equals(Void.class) || type.equals(void.class);
    }

}
