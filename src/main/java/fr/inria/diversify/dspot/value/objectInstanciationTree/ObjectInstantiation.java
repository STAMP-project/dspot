package fr.inria.diversify.dspot.value.objectInstanciationTree;


import fr.inria.diversify.dspot.value.PrimitiveValue;
import fr.inria.diversify.dspot.value.Value;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.dspot.value.ValueType;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * User: Simon
 * Date: 19/08/16
 */
public class ObjectInstantiation extends Value {
    protected Boolean isOk = null;

    protected static Random random = new Random();
    protected CtExecutableReference constructor;
    protected String constructorString;
    protected List<List<Value>> params;


    public ObjectInstantiation(ValueType type, CtExecutableReference constructor, ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        this.type = type;
        this.constructor = constructor;
        this.constructorString = this.constructor.toString();
        this.params = new ArrayList<>(constructor.getParameters().size());
        IntStream.range(0, constructor.getParameters().size())
                .forEach(i -> params.add(new ArrayList<>()));
    }

    @Override
    public String getDynamicType() {
        return type.getType();
    }

    public CtTypeReference getType() {
        return constructor.getType();
    }

    public CtExecutableReference getConstructor() {
        return constructor;
    }

    public boolean isOk() {
        if(isOk == null) {
            List<CtTypeReference<?>> args = constructor.getParameters();

            IntStream.range(0, params.size())
                    .filter(i -> params.get(i).isEmpty())
                    .forEach(i -> {
                        ValueType type = valueFactory.getValueType(args.get(i));
                        params.get(i).addAll(type.getAll(true));
                    });

            isOk = params.stream()
                    .allMatch(values -> !values.isEmpty()
                            && values.stream()
                            .anyMatch(p -> p != this && p.isOk()));
        }
        return isOk;
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

    public CtExpression getValue() {
        CtExpression[] paramValues = params.stream()
                .map(list -> {
                    if(list.isEmpty()) {
                        return null;
                    } else {
                        return list.get(random.nextInt(list.size()));
                    }
                } )
                .map(value -> value.getValue())
                .collect(Collectors.toList()).toArray(new CtExpression[params.size()]);

        CtConstructorCall constructorCall = constructor.getFactory().Code().createConstructorCall(getType(), paramValues);

        return constructorCall;
    }

    @Override
    public void initLocalVar(CtBlock body, CtLocalVariable localVar) throws Exception {
        localVar.setDefaultExpression(getValue());
    }

    public void addParameterValue(Value value, Integer paramIndex) {
        params.get(paramIndex).add(value);
    }

    public List<CtTypeReference<?>> getParameters() {
        return constructor.getParameters();
    }
}
