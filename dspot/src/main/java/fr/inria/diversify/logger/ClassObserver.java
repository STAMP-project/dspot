package fr.inria.diversify.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: Simon
 * Date: 16/06/15
 * Time: 11:09
 */
public class ClassObserver {
    protected Class aClass;
    protected Field[] fields;
    protected Method[] getters;
    protected String[] previousObservation;

    private static Map<Class, String> classToId;

    public ClassObserver(Class aClass) {
        this.aClass = aClass;
    }

    protected void observe(Object object, PrintWriter writer) throws IOException, InterruptedException {
        if(fields == null) {
            intGettersAndFields(writer);
        }

        String[] results = new String[getters.length + fields.length];

        int i = 0;
        for(; i < getters.length ; i++) {
            try {
                results[i] = formatVar(getters[i].invoke(object));
            } catch (Exception e) {
                results[i] = "null";
            }
        }
        int j= 0;
        for(; i < results.length ; i++) {
            try {
                results[i] = formatVar(fields[j].get(object));
            } catch (Exception e) {
                results[i] = "null";
            }
            j++;
        }
        StringBuilder sameValue = new StringBuilder();
        List<String> result = new ArrayList<String>();
        boolean sameValues = true;
        if(previousObservation != null) {
            for (i = 0; i < results.length; i++) {
                if (previousObservation[i].equals(results[i])) {
                    sameValue.append("0");
                } else {
                    sameValues = false;
                    sameValue.append("1");
                    result.add(results[i]);
                    previousObservation[i] = results[i];
                }
            }
        } else {
            sameValues = false;
            previousObservation = new String[results.length];
            for (i = 0; i < results.length; i++) {
                sameValue.append("1");
                result.add(results[i]);
                previousObservation[i] = results[i];
            }
        }

        String classId = getClassId(writer);

        if(sameValues) {
            writer.write(classId);
        } else {
            writer.write( classId + KeyWord.simpleSeparator + sameValue.toString() + KeyWord.separator + join(result, KeyWord.separator));
        }
    }

    protected String getClassId(PrintWriter writer) throws IOException, InterruptedException {
        String className;
        if(aClass == null) {
            className = "NullClass";
        } else {
            className = aClass.getName();
        }
        int count = classToId.size() + 1;
        if(!classToId.containsKey(aClass)) {
            classToId.put(aClass,  count + "");
            writer.append(KeyWord.endLine + "\n" + KeyWord.classKeyWord + KeyWord.simpleSeparator + className + KeyWord.simpleSeparator + count);
        }
        return classToId.get(aClass);
    }

    protected String join(List<String> list, String conjunction)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list)
        {
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }

    protected String formatVar(Object object) {
        if (object == null) {
            return "null";
        }
        String string;
        if (object.getClass().isArray()) {
            string = Arrays.toString((Object[]) object);
        } else {
            string = object + "";
        }
        if(string.length() > 1000)
            string = string.length() + "";

        return string;
    }

    protected void intGettersAndFields(PrintWriter writer) throws IOException, InterruptedException {
        if(aClass == null) {
            getters = new Method[0];
            fields = new Field[0];
        } else {
            getters = findGetters();
            fields = findFields();
        }

        String classId = getClassId(writer);

        writer.append(KeyWord.endLine + "\n" + KeyWord.getterKeyWord + KeyWord.simpleSeparator + classId);
        for(Method method : getters) {
            writer.append(KeyWord.simpleSeparator + method.getName());
        }
        for(Field field : fields) {
            writer.append(KeyWord.simpleSeparator + field.getName());
        }
    }

    protected Field[] findFields() {
        List<Field> fields = new ArrayList<Field>();
        for(Field field : aClass.getFields()) {
            if(Modifier.isPublic(field.getModifiers())) {
                fields.add(field);
            }
        }
        Field[] ret = new Field[fields.size()];
        for(int i = 0; i < fields.size(); i++) {
            ret[i] = fields.get(i);
        }
        return ret;
    }

    protected Method[] findGetters(){
        List<Method> getters = new ArrayList<Method>();
        for(Method method : aClass.getMethods()){
            if((isGetter(method) || isIs(method)) && !methodDefinedInObject(method)) {
                getters.add(method);
            }
        }

        try {
            Method toStringMethod = aClass.getMethod("toString");
            if(!methodDefinedInObject(toStringMethod)) {
                getters.add(toStringMethod);
            }
        } catch (NoSuchMethodException e) {}

        Method[] ret = new Method[getters.size()];

        for(int i = 0; i< ret.length; i++ ) {
            ret[i] = getters.get(i);
        }
        return ret;
    }

    protected boolean isIs(Method method) {
        return method.getName().startsWith("is")
                && method.getParameterTypes().length == 0;
    }

    protected boolean isGetter(Method method) {
        return method.getName().startsWith("get")
                && method.getParameterTypes().length == 0;
    }

    protected boolean methodDefinedInObject(Method method) {
        for(Method objectMethod : Object.class.getMethods()) {
            if(objectMethod.equals(method)) {
                return true;
            }
        }
        return false;
    }
}
