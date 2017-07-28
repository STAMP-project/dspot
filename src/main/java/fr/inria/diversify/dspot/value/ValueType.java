package fr.inria.diversify.dspot.value;


import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.TypeUtils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: Simon
 * Date: 18/10/16
 * Time: 15:05
 */
public class ValueType {

    private CtTypeReference type;

    public ValueType(CtTypeReference type) {
        this.type = type;
    }

    public CtExpression<?> getRandomValue() {
        if (AmplificationChecker.isPrimitive(this.type)) {
            return generatePrimitiveRandomValue();
        } else {
            //todo
            /*CtExecutableReference constructor = getEmptyConstructor();
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
            }*/
        }
        return null;
    }

    private CtExpression<?> generatePrimitiveRandomValue() {
        if(type.getActualClass() == Boolean.class || type.getActualClass() == boolean.class) {
            return type.getFactory().createLiteral(AmplificationHelper.getRandom().nextBoolean());
        }
        if(type.getActualClass() == Character.class ||type.getActualClass() == char.class) {
            return type.getFactory().createLiteral(AmplificationHelper.getRandomChar());
        }
        if(type.getActualClass() == Byte.class || type.getActualClass() == byte.class) {
            return type.getFactory().createLiteral((byte) AmplificationHelper.getRandom().nextInt(100));
        }
        if(type.getActualClass() == Short.class || type.getActualClass() == short.class) {
            return type.getFactory().createLiteral((short) AmplificationHelper.getRandom().nextInt(100));
        }
        if(type.getActualClass() == Integer.class || type.getActualClass() == int.class) {
            return type.getFactory().createLiteral((AmplificationHelper.getRandom().nextInt(100)));
        }
        if(type.getActualClass() == Long.class || type.getActualClass() == long.class) {
            return type.getFactory().createLiteral((long) AmplificationHelper.getRandom().nextInt(100));
        }
        if(type.getActualClass() == Float.class || type.getActualClass() == float.class) {
            return type.getFactory().createLiteral((float) AmplificationHelper.getRandom().nextDouble());
        }
        if(type.getActualClass() == Double.class || type.getActualClass() == double.class) {
            return type.getFactory().createLiteral(AmplificationHelper.getRandom().nextDouble());
        }
        if(type.getActualClass() == String.class) {
            return type.getFactory().createLiteral(AmplificationHelper.getRandomString(20));
        }
        throw new RuntimeException();
    }
}
