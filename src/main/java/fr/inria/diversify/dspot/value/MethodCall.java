package fr.inria.diversify.dspot.value;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.*;

/**
 * User: Simon
 * Date: 31/08/16
 * Time: 14:31
 */
public class MethodCall {
    protected Factory factory;
    protected CtMethod method;
    protected String methodName;
    protected List<Value> parameterValues;

    protected CtClass target;

    public MethodCall(Factory factory) {
        this.factory = factory;
        parameterValues = new ArrayList<>();
    }

    public void addParameter(Value value) {
        parameterValues.add(value);
    }

    public void setMethod(String method) {
        findMethod(method);
    }

    public void setTarget(String targetName) {
        if(!targetName.equals("static")) {
            target = factory.Class().get(targetName);
        }
    }

    protected static Map<String, CtMethod> cache = new HashMap<>();
    protected void findMethod(String methodName) {
        this.methodName = methodName;
        if(!cache.containsKey(methodName)) {
            int index = methodName.lastIndexOf(".");
            String className = methodName.substring(0, index);
            String simpleMethodName = methodName.substring(index + 1, methodName.length());

            CtClass cl = factory.Class().get(className);

            Set<CtMethod> methods = cl.getMethods();
            cache.put(methodName,
                    methods.stream()
                        .filter(mth -> mth.getSimpleName().equals(simpleMethodName))
                        .filter(mth -> mth.getParameters().size() == parameterValues.size())
                        .findFirst()
                        .orElse(null));
        }
        method = cache.get(methodName);
    }

    public CtMethod getMethod() {
        return method;
    }

    public List<Value> getParameterValues() {
        return parameterValues;
    }

    public CtClass getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodCall that = (MethodCall) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        return parameterValues != null ? parameterValues.equals(that.parameterValues) : that.parameterValues == null;

    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + (parameterValues != null ? parameterValues.hashCode() : 0);
        return result;
    }
}
