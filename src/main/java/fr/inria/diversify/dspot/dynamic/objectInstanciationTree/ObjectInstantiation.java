package fr.inria.diversify.dspot.dynamic.objectInstanciationTree;


import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 19/08/16
 */
public class ObjectInstantiation extends AbstractObjectInstantiation {
    protected CtExecutableReference constructor;
    protected String constructorString;
    protected String className;
    protected Map<Integer, AbstractObjectInstantiation> params;


    public ObjectInstantiation(CtConstructorCall constructor) {
        this.constructor = constructor.getExecutable();
        this.className = this.constructor.getDeclaringType().getQualifiedName();
        constructorString = this.constructor.toString();
        params = new HashMap<>();
    }

    private boolean hasEmptyConstructor(CtTypeReference<?> typeRef) {
        try {
            if(typeRef.getDeclaration() != null) {
                return ((CtClass) typeRef.getDeclaration()).getConstructor() != null;
            } else {
                typeRef.getActualClass().getConstructor();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }


    public void update(Set<ObjectInstantiation> objectInstantiations) {
        if(constructor.getParameters().size() != 0) {
            List<CtTypeReference<?>> p = constructor.getParameters();
            List<String> paramsName = p.stream()
                    .map(pp -> pp.toString())
                    .collect(Collectors.toList());
            objectInstantiations.stream()
                    .filter(objectInstantiation -> objectInstantiation != this)
                    .forEach(objectInstantiation -> {
                        if(params.size() != p.size()) {
                            for (int i = 0; i < p.size(); i++) {
                                if (!params.containsKey(i)) {
                                    if (objectInstantiation.getClassName().equals(paramsName.get(i))) {
                                        params.put(i, objectInstantiation);
                                    }
                                }
                                i++;
                            }
                        }
                    });
        }
    }

    public CtTypeReference getType() {
        return constructor.getType();
    }

    public CtExecutableReference getConstructor() {
        return constructor;
    }

    public String toDot() {
        String ret;
        if(isOk()) {
            ret = this.hashCode() + "[label=" + getType().getSimpleName() + "];\n";
        } else {
            ret = this.hashCode() + "[color=red, label=" + getType().getSimpleName() + "];\n";
        }
        return ret + params.entrySet().stream()
                .map(p -> this.hashCode() + " -> " + p.getValue().hashCode() + " [label=\"O " + p.getKey() + "\"];\n")
                .collect(Collectors.joining(""));
    }

    public boolean isOk() {
        Factory factory = constructor.getFactory();
        int idx[] = {-1};
        List<CtTypeReference<?>> args = constructor.getActualTypeArguments();

        args.stream()
                .peek(typeRef -> idx[0]++)
                .filter(typeRef -> !params.containsKey(idx[0]))
                .filter(typeRef -> hasEmptyConstructor(typeRef))
                .map(typeRef -> factory.Code().createConstructorCall(typeRef))
                .forEach(constructorCall -> params.put(idx[0], new ObjectInstantiation((CtConstructorCall) constructorCall)));
        if(params.size() == constructor.getParameters().size()) {
            return params.values().stream().allMatch(p -> p != this && p.isOk());
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectInstantiation that = (ObjectInstantiation) o;

        return constructorString.equals(that.constructorString);

    }

    @Override
    public int hashCode() {
        return constructorString.hashCode() * 43;
    }

    public void updatePrimitive(Map<String, PrimitiveInstantiation> primitiveInstantiations) {
        if(constructor.getParameters().size() != 0) {
            primitiveInstantiations.keySet().stream()
                    .filter(key -> key.startsWith(constructorString))
                    .forEach(key -> {
                        int paramIndex = Integer.parseInt(key.substring(constructorString.length() + 1, key.length()));
                        params.put(paramIndex, primitiveInstantiations.get(key));
                    });

        }
    }

    public CtExpression getValue() {
        CtExpression[] paramValues = params.keySet().stream()
                .mapToInt(i -> i)
                .sorted()
                .mapToObj(i -> params.get(i).getValue())
                .collect(Collectors.toList()).toArray(new CtExpression[params.size()]);

        CtConstructorCall constructorCall = constructor.getFactory().Code().createConstructorCall(getType(), paramValues);

        return constructorCall;
    }


    public String getConstructorString() {
        return constructorString;
    }

    public String getClassName() {
        return className;
    }
}
