package fr.inria.diversify.dspot.dynamic.objectInstanciationTree;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * User: Simon
 * Date: 25/08/16
 * Time: 17:07
 */
public class PrimitiveInstantiation extends AbstractObjectInstantiation {
    protected CtTypeReference typeRef;
    protected List<Object> values;

    public PrimitiveInstantiation(CtTypeReference typeRef) {
        this.typeRef = typeRef;
        values = new ArrayList<>();
    }

    @Override
    public CtTypeReference getType() {
        return typeRef;
    }

    @Override
    public void update(Set<ObjectInstantiation> objectInstantiations) {

    }

    @Override
    public boolean isOk() {
        return true;
    }

    public void addValue(String value) {
        if(!values.contains(value)) {
            Class cl = typeRef.getActualClass();
            if(cl.isAssignableFrom(Integer.class) || cl.isAssignableFrom(int.class)) {
                values.add(Integer.parseInt(value));
            } else if(cl.isAssignableFrom(Long.class) || cl.isAssignableFrom(long.class)) {
                values.add(Long.parseLong(value));
            } else if(cl.isAssignableFrom(Double.class) || cl.isAssignableFrom(double.class)) {
                values.add(Double.parseDouble(value));
            } else if(cl.isAssignableFrom(Short.class) || cl.isAssignableFrom(short.class)) {
                values.add(Short.parseShort(value));
            } else if(cl.isAssignableFrom(Float.class) || cl.isAssignableFrom(float.class)) {
                values.add(Float.parseFloat(value));
            } else if(cl.isAssignableFrom(Byte.class) || cl.isAssignableFrom(byte.class)) {
                values.add(Byte.parseByte(value));
            } else if(cl.isAssignableFrom(Boolean.class) || cl.isAssignableFrom(boolean.class)) {
                values.add(Boolean.parseBoolean(value));
            } else {
                values.add(value);
            }
            values.add(value);
        }
    }

    public CtExpression getValue() {
        Random r = new Random();
        Object value = values.get(r.nextInt(values.size()));
        CtLiteral literal = typeRef.getFactory().Code().createLiteral(value);
        literal.setType(typeRef);
        return literal;
    }
}
