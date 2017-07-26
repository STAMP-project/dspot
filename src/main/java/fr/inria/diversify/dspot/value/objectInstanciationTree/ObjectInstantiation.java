package fr.inria.diversify.dspot.value.objectInstanciationTree;


import fr.inria.diversify.dspot.value.Value;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.dspot.value.ValueType;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * User: Simon
 * Date: 19/08/16
 */
public class ObjectInstantiation extends Value {

    private Boolean isOk = null;
    private CtExecutableReference constructor;
    private String constructorString;
    private List<List<Value>> params;

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
                        return list.get(AmplificationHelper.getRandom().nextInt(list.size()));
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

}
