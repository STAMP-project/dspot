package fr.inria.diversify.dspot.value;


import fr.inria.diversify.dspot.value.objectInstanciationTree.ObjectInstantiation;
import fr.inria.diversify.utils.TypeUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * User: Simon
 * Date: 18/10/16
 * Time: 15:05
 */
public class ValueType {
    protected static Factory factory;
    protected static ValueFactory valueFactory;
    protected static Random random = new Random();
    protected String dynamicType;
    protected List<Value> values;



    public ValueType(String dynamicType) {
        this.dynamicType = dynamicType;
        this.values = new ArrayList<>();
    }

    public void addValue(Value value) {
        if(!values.contains(value)) {
            values.add(value);
        }
    }

    public Value getRandomValue(boolean generateIsEmpty) {
        while (!values.isEmpty()) {
            int index = random.nextInt(values.size());
            Value value = values.get(index);
            if(value.isOk()) {
                return value;
            } else {
                values.remove(value);
            }
        }
        if(generateIsEmpty) {
            return generateRandomValue();
        } else {
            return null;
        }
    }

    public List<Value> getAll(boolean generateIsEmpty) {
        if(generateIsEmpty && values.isEmpty()) {
            Value random = generateRandomValue();
            if(random != null) {
                values.add(random);
            }
        }
        return values;
    }

    public String getType() {
        return dynamicType;
    }

    protected Value generateRandomValue() {
//        if(dynamicType.equals("null")) {
//        }
        Class<?> type = null;
        try {
            type = Class.forName(dynamicType);
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
        }
        Object primitiveValue = null;
        if(type == Boolean.class) {
            primitiveValue = random.nextBoolean();
        }
        if(type == Character.class) {
            primitiveValue = '1';
        }
        if(type == Byte.class) {
//            value = '1';
        }
        if(type == Short.class) {
            primitiveValue = (short)random.nextInt(100);
        }
        if(type == Integer.class) {
            primitiveValue = random.nextInt(100);
        }
        if(type == Long.class) {
            primitiveValue = (long)random.nextInt(100);
        }
        if(type == Float.class) {
            primitiveValue = (float)random.nextDouble();
        }
        if(type == Double.class) {
            primitiveValue = random.nextDouble();
        }
        if(type == String.class) {
            primitiveValue = "foo";
        }

        if(primitiveValue != null) {
            return new PrimitiveValue(this, primitiveValue + "");
        } else {
            CtExecutableReference constructor = getEmptyConstructor();
            if(constructor != null) {
                Value value = new ObjectInstantiation(this, constructor, valueFactory);
                if(value.isOk()) {
                    addValue(value);
                    return value;
                }
            }
        }
        return null;
    }

    public boolean hasValue() {
        return !values.isEmpty();
    }

    protected CtExecutableReference getEmptyConstructor() {
        try {
            CtClass cl = factory.Class().get(dynamicType);
            if(cl != null) {
                if(cl.getConstructor() != null) {
                    return cl.getConstructor().getReference();
                } else {
                    Set<CtConstructor> constructors = cl.getConstructors();
                    CtExecutableReference exeRef = constructors.stream()
                            .filter(constructor -> {
                                List<CtParameter> parameters = constructor.getParameters();
                                return parameters.stream()
                                        .allMatch(type -> TypeUtils.isSerializable(type.getType()));
                            })
                            .map(constructor -> constructor.getReference())
                            .findFirst()
                            .orElse(null);

                    if(exeRef != null) {
                        //todo
                    }
                    return exeRef;
                }
            } else {
                Class type = Class.forName(dynamicType);
//                factory.Executable().createReference("gc")
                return null;
            }

        } catch (Exception e) {}
        return null;
    }

    public Factory getSpoonFactory() {
        return factory;
    }

    public static void setFactory(Factory factory, ValueFactory valueFactory) {
        ValueType.factory = factory;
        ValueType.valueFactory = valueFactory;
    }
}
