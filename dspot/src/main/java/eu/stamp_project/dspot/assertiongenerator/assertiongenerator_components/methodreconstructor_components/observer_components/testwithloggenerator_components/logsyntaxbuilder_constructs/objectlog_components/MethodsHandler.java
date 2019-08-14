package eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.methodreconstructor_components.observer_components.testwithloggenerator_components.logsyntaxbuilder_constructs.objectlog_components;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.BaseStream;


public class MethodsHandler {

    private Map<Class<?>, List<Method>> cache;

    @Deprecated
    private static final List<String> forbiddenMethods;

    private static final List<String> forbiddenClasses;

    private static final List<String> forbiddenPackages;

    static {
        forbiddenMethods = new ArrayList<>();
        forbiddenMethods.add("equals");
        forbiddenMethods.add("notify");
        forbiddenMethods.add("notifyAll");
        forbiddenMethods.add("wait");
        forbiddenMethods.add("getClass");
        forbiddenMethods.add("display");
        forbiddenMethods.add("clone");
        forbiddenMethods.add("hasExtensions");
        forbiddenMethods.add("hashCode");
        forbiddenMethods.add("toString");

        // since we generate contains(), we don't need to observe iterators
        forbiddenMethods.add("iterator");
        forbiddenMethods.add("spliterator");
        forbiddenMethods.add("listIterator");
        forbiddenMethods.add("stream");
        forbiddenMethods.add("parallelStream");
        forbiddenMethods.add("reverse");
        forbiddenMethods.add("clear");

        forbiddenClasses = new ArrayList<>();
        forbiddenClasses.add("java.lang.Object");
        forbiddenClasses.add("java.lang.Class");
        forbiddenClasses.add("java.lang.Enum");
        forbiddenClasses.add("java.util.Date");
        forbiddenClasses.add("java.net.URL");
        forbiddenClasses.add("java.util.Calendar");
        forbiddenClasses.add("java.io.File");

        forbiddenPackages = new ArrayList<>();
        forbiddenPackages.add("java.time");
    }

    public MethodsHandler() {
        this.cache = new HashMap<>();
    }

    private boolean matchOnForbiddenPackage(String className) {
        return forbiddenPackages.stream()
                .anyMatch(className::startsWith);
    }

    public List<Method> getAllMethods(Class<?> clazz) {
        if (forbiddenClasses.contains(clazz.getName()) ||
            this.matchOnForbiddenPackage(clazz.getName())
        ) {
            return Collections.emptyList();
        }
        if (!cache.containsKey(clazz)) {
            findMethods(clazz);
        }
        return cache.get(clazz);
    }

    private void findMethods(Class<?> clazz) {
        List<Method> methodsList = new ArrayList<Method>();
        for (Method m : clazz.getMethods()) {
            if (isValidMethod(m)) {
                methodsList.add(m);
            }
        }
        cache.put(clazz, methodsList);
    }

    @Deprecated // since we forbid the class Object
    boolean isDefaulttoStringOrHashCode(Method method) {
        if (method.getDeclaringClass() == null) {
            return false;
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        if ("hashCode".equals(method.getName())) {
            return isDefaultClass(declaringClass.getName()) ||
                    declaringClass.getName().equals("java.net.URL") ||
                    declaringClass.getName().equals("java.net.URI");
        } else {
            return "toString".equals(method.getName()) &&
                    isDefaultClass(declaringClass.getName());
        }
    }

    @Deprecated
    private boolean isDefaultClass(String qualifiedName) {
        return ("java.lang.Enum".equals(qualifiedName) || "java.lang.Object".equals(qualifiedName));
    }

    boolean isValidMethod(Method method) {
        if (!Modifier.isPublic(method.getModifiers())  // the method is not public
                || method.getParameterCount() > 0 // there is parameters
                || Modifier.isStatic(method.getModifiers()) // the method is static
                || isVoid(method.getReturnType()) // the method is return void type, i.e. it returns nothing
                || method.getReturnType() == Class.class // the method returns Class<?>
                || !Modifier.isPublic(method.getReturnType().getModifiers())  // the method return a type that is not visible, i.e. is not public.
                || forbiddenClasses.contains(method.getReturnType().getName()) // it returns a forbidden type
                || returnStream(method) // the method return a stream
                || method.getParameterTypes().length > 0 // the method hasParameter
                || isDefaulttoStringOrHashCode(method) // we don't use default implementation of toString() and hashCode, i.e. implementation of Object and Enum
                ) {
            return false;
        }

        // we rely on convention name: get, is, should
        // TODO expand the scope of assertion to other pure method, with return type etc...

        return isASupportedMethodName(method.getName());
    }

    public static boolean isASupportedMethodName(String name) {
        return name.startsWith("has") ||
                name.startsWith("get") ||
                name.startsWith("is") ||
                name.startsWith("should") ||
                name.equals("toString") ||
                name.startsWith("hashCode");
    }

    private boolean returnStream(Method method) {
        try {
            return BaseStream.class.isAssignableFrom(method.getReturnType());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isVoid(Class<?> type) {
        return type.equals(Void.class) || type.equals(void.class);
    }

}
