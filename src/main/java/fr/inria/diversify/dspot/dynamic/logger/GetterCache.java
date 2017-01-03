package fr.inria.diversify.dspot.dynamic.logger;

import fr.inria.diversify.dspot.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * User: Simon
 * Date: 09/09/16
 * Time: 11:33
 */
public class GetterCache {
    protected Map<Class, List<Field>> directAccess;
    protected Map<Class, Map<Method, Method>> accessMethods;

    public GetterCache() {
        directAccess = new HashMap<Class, List<Field>>();
        accessMethods = new HashMap<Class, Map<Method, Method>>();
    }

    public List<Field> getDirectAccess(Class cl) {
        if(!directAccess.containsKey(cl)) {
            computeGetter(cl);
        }
        return directAccess.get(cl);
    }

    public Map<Method, Method> getAccessMethod(Class cl) {
        if(!directAccess.containsKey(cl)) {
            computeGetter(cl);
        }
        return accessMethods.get(cl);
    }

    protected void computeGetter(Class cl) {
        Field[] fields = cl.getFields();

        List<Field> set = new LinkedList<Field>();
        directAccess.put(cl, set);

        for(int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if(TypeUtils.isPrimitive(field.getType()) && hasDirectAccess(field) && !IsStatic(field)) {
                set.add(field);
            }
        }

        Map<Method, Method> map = new HashMap<Method, Method>();
        accessMethods.put(cl, map);

        List<Method> setMethods = findSetMethods(cl.getMethods());
        List<Method> getMethods = findGetMethods(cl.getMethods());

        for(int i = 0; i < getMethods.size(); i++) {
            for (int j = 0; j < setMethods.size(); j++) {
                String getName = getMethods.get(i).getName().substring(3,getMethods.get(i).getName().length());
                String setName = setMethods.get(j).getName().substring(3,setMethods.get(j).getName().length());
                if (getName.equals(setName)) {
                    map.put(getMethods.get(i), setMethods.get(j));
                    break;
                }
            }
        }
    }

    private boolean IsStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    protected boolean hasDirectAccess(Field field) {
        return Modifier.isPublic(field.getModifiers());
    }

    protected List<Method> findGetMethods(Method[] methods) {
        List<Method> list = new LinkedList<Method>();
        for (int i = 0; i < methods.length; i++) {
            Method mth = methods[i];
            if (isPublicMethod(mth)
                    && TypeUtils.isPrimitive(mth.getReturnType())
                    && mth.getName().startsWith("get")
                    && !voidReturnType(mth)
                    && mth.getParameterCount() == 0) {
                list.add(mth);
            }
        }
        return list;
    }

    protected List<Method> findSetMethods(Method[] methods) {
        List<Method> list = new LinkedList<Method>();
        for (int i = 0; i < methods.length; i++) {
            Method mth = methods[i];
            if (isPublicMethod(mth)
                    && TypeUtils.isPrimitive(mth.getReturnType())
                    && mth.getName().startsWith("set")
                    && voidReturnType(mth)
                    && mth.getParameterCount() == 1) {
                list.add(mth);
            }
        }
        return list;
    }


    protected boolean isPublicMethod(Method mth) {
        return Modifier.isPublic(mth.getModifiers());
    }

    protected boolean voidReturnType(Method mth) {
        return mth.getReturnType().equals(void.class) || mth.getReturnType().equals(Void.class);
    }
}
