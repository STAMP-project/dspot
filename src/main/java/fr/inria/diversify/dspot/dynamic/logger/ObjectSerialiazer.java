package fr.inria.diversify.dspot.dynamic.logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Simon
 * Date: 08/09/16
 * Time: 16:33
 */
public class ObjectSerialiazer {
    protected static GetterCache getterCache = new GetterCache();
    protected Class type;
    protected Map<String, Class> fieldTypes;
    protected Map<String, String> fieldValues;
    protected Object object;



    public ObjectSerialiazer(Object object) {
        this.object = object;
        if (object != null) {
            type = object.getClass();

            fieldTypes = new HashMap<String, Class>();
            fieldValues = new HashMap<String, String>();

            List<Field> directAccess = getterCache.getDirectAccess(type);
            for (int i = 0; i < directAccess.size(); i++) {
                Field field = directAccess.get(i);
                try {
                    Object value = field.get(object);
                    fieldTypes.put(field.getName(), getType(field, value));
                    fieldValues.put(field.getName(), "" + value);
                } catch (IllegalAccessException e) {}
            }

            Map<Method, Method> accessMethods = getterCache.getAccessMethod(type);
            for (Map.Entry<Method, Method> entry : accessMethods.entrySet()) {
                try {
                    Object value = entry.getKey().invoke(object);
                    fieldTypes.put(entry.getValue().getName() + "(", value.getClass());
                    fieldValues.put(entry.getValue().getName() + "(", "" + value);
                } catch (Exception e) {}
            }
        }
    }

    public Class getType(Field field, Object value) {
        if(value == null) {
            return field.getType();
        } else {
            return value.getClass();
        }
    }

    public Set<String> getField() {
        return fieldValues.keySet();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getCanonicalName());
        for (String fieldOrMethod: fieldValues.keySet()) {
            builder.append(KeyWord.endLine)
                    .append(KeyWord.ObjectParameterField)
                    .append(KeyWord.simpleSeparator)
                    .append(fieldOrMethod)
                    .append(KeyWord.simpleSeparator)
                    .append(fieldTypes.get(fieldOrMethod).getCanonicalName())
                    .append(KeyWord.simpleSeparator)
                    .append(fieldValues.get(fieldOrMethod));
        }
        return builder.toString();
    }

    public Class getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }
}
