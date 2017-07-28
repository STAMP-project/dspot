package fr.inria.diversify.dspot.value;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.stream.IntStream;

/**
 * User: Simon
 * Date: 07/01/16
 * Time: 10:40
 */
public class ValueCreator {

    private static final int MAX_ARRAY_SIZE = 5;
    private int count;

    public ValueCreator() {
        this.count = 0;
    }

    public CtLocalVariable createRandomLocalVar(CtTypeReference type) {
        Factory factory = type.getFactory();
        CtExpression value = createValue(type);
        if (value != null) {
            return factory.Code().createLocalVariable(type, "vc_" + count++, value);
        } else {
            return null;
        }
    }

    public CtLocalVariable createNull(CtTypeReference type) {
        Factory factory = type.getFactory();
        final CtLiteral<?> defaultExpression = factory.createLiteral(null);
        defaultExpression.addTypeCast(type);
        return factory.Code().createLocalVariable(type, "vc_" + count++, defaultExpression);
    }

    public CtExpression createValue(CtTypeReference type) {
        Factory factory = type.getFactory();
        if (AmplificationChecker.isPrimitive(type)) {
            return createRandomPrimitive(type);
        } else if (AmplificationChecker.isArray(type)) {
            CtArrayTypeReference arrayType = (CtArrayTypeReference) type;
            CtTypeReference typeComponent = arrayType.getComponentType();
            final CtNewArray<?> newArray = factory.createNewArray();
            newArray.setType(typeComponent);
            IntStream.range(0, AmplificationHelper.getRandom().nextInt(MAX_ARRAY_SIZE))
                    .mapToObj(i -> createValue(typeComponent))
                    .forEach(newArray::addElement);
            return newArray;
        } else {
            CtConstructorCall<?> constructorCall = factory.createConstructorCall();
            constructorCall.setType(type);
            return constructorCall;
        }
    }

    private CtLiteral createRandomPrimitive(CtTypeReference type) {
        Factory factory = type.getFactory();
        String typeName = type.unbox().getSimpleName();
        switch (typeName) {
            case "int":
                return factory.Code().createLiteral(AmplificationHelper.getRandom().nextInt());
            case "long":
                return factory.Code().createLiteral(AmplificationHelper.getRandom().nextLong());
            case "float":
                return factory.Code().createLiteral(AmplificationHelper.getRandom().nextFloat());
            case "double":
                return factory.Code().createLiteral(AmplificationHelper.getRandom().nextDouble());
            case "boolean":
                return factory.Code().createLiteral(AmplificationHelper.getRandom().nextBoolean());
            case "short":
                return factory.Code().createLiteral(AmplificationHelper.getRandom().nextInt(Short.MAX_VALUE));
            case "byte":
                return factory.Code().createLiteral(AmplificationHelper.getRandom().nextInt(Byte.MAX_VALUE));
            case "char":
                return factory.Code().createLiteral((char) ((byte) AmplificationHelper.getRandom().nextInt(Byte.MAX_VALUE)));
        }
        return null;
    }
}
