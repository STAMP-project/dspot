package fr.inria.diversify.dspot.value;


import fr.inria.diversify.dspot.amp.AmplifierHelper;
import fr.inria.diversify.dspot.value.objectInstanciationTree.ObjectInstantiation;
import fr.inria.diversify.dspot.value.objectInstanciationTree.StaticMethodValue;
import fr.inria.diversify.utils.CtTypeUtils;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;

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
            int index = AmplifierHelper.getRandom().nextInt(values.size());
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


    //TODO
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
            primitiveValue = AmplifierHelper.getRandom().nextBoolean();
        }
        if(type == Character.class) {
            primitiveValue = '1';
        }
        if(type == Byte.class) {
//            value = '1';
        }
        if(type == Short.class) {
            primitiveValue = (short)AmplifierHelper.getRandom().nextInt(100);
        }
        if(type == Integer.class || type == int.class) {
            primitiveValue = AmplifierHelper.getRandom().nextInt(100);
        }
        if(type == Long.class) {
            primitiveValue = (long)AmplifierHelper.getRandom().nextInt(100);
        }
        if(type == Float.class) {
            primitiveValue = (float)AmplifierHelper.getRandom().nextDouble();
        }
        if(type == Double.class) {
            primitiveValue = AmplifierHelper.getRandom().nextDouble();
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
            } else {
                CtExecutableReference mth = getSingletonMethodAccess();
                if(mth != null) {
                    Value value = new StaticMethodValue(this, mth, valueFactory);
                    addValue(value);
                    return value;
                }
            }
        }
        return null;
    }

    protected CtExecutableReference getSingletonMethodAccess() {
        try {
            CtClass cl = factory.Class().get(dynamicType);
            if (cl != null) {
                Set<CtMethod> methods = cl.getMethods();
                return methods.stream()
                        .filter(mth -> mth.getModifiers().contains(ModifierKind.PUBLIC))
                        .filter(mth -> mth.getModifiers().contains(ModifierKind.STATIC))
                        .filter(mth -> mth.getParameters().isEmpty())
                        .filter(mth -> mth.getType().getQualifiedName().equals(dynamicType))
                        .map(mth -> mth.getReference())
                        .findAny()
                        .orElse(null);
            }
        } catch (Exception e) {}
        return null;
    }

    protected CtExecutableReference getEmptyConstructor() {
        try {
            CtClass cl = factory.Class().get(dynamicType);
            if(cl != null) {
                CtConstructor constructor = cl.getConstructor();
                if(constructor != null && !isPrivate(constructor)) {
                    return cl.getConstructor().getReference();
                } else {
                    Set<CtConstructor> constructors = cl.getConstructors();
                    CtExecutableReference exeRef = constructors.stream()
                            .filter(c -> !isPrivate(c))
                            .filter(c -> {
                                List<CtParameter> parameters = c.getParameters();
                                return parameters.stream()
                                        .allMatch(type -> CtTypeUtils.isSerializable(type.getType()));
                            })
                            .map(c -> c.getReference())
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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isPrivate(CtConstructor constructor) {
        return constructor.getModifiers().contains(ModifierKind.PRIVATE);
    }

    public Factory getSpoonFactory() {
        return factory;
    }

    public static void setFactory(Factory factory, ValueFactory valueFactory) {
        ValueType.factory = factory;
        ValueType.valueFactory = valueFactory;
    }
}
