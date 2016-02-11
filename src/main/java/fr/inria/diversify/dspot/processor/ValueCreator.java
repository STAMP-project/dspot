package fr.inria.diversify.dspot.processor;

import fr.inria.diversify.util.Log;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * User: Simon
 * Date: 07/01/16
 * Time: 10:40
 */
public class ValueCreator {
    protected Random random;
    protected int maxArraySize = 5;
    protected static int count;


    public ValueCreator() {
        random = new Random();
    }

    public CtLocalVariable createRandomLocalVar(CtTypeReference type) {
        Factory factory = type.getFactory();
        CtExpression value = createValue(type);
       try {


           if (value != null) {
               return factory.Code().createLocalVariable(type, "vc_" + count++, createValue(type));
           } else {
               return null;
           }
       } catch (Exception e) {
           e.printStackTrace();
           Log.debug("");
       }
        return null;
    }

    public CtLocalVariable createNull(CtTypeReference type) {
        Factory factory = type.getFactory();
        String snippet = "(" + type.getQualifiedName()+")null";

        CtExpression expression = factory.Code().createCodeSnippetExpression(snippet);
        expression.setType(type);

        return factory.Code().createLocalVariable(type, "vc_"+count++, expression);
    }

    public CtExpression createValue(CtTypeReference type) {
        try {
        if(isPrimitive(type)) {
            return createRandomPrimitive(type);
        }

        Factory factory = type.getFactory();
        String snippet = null;
        if(isArray(type)) {
            CtArrayTypeReference arrayType = (CtArrayTypeReference) type;
            CtTypeReference typeComponent = arrayType.getComponentType();
            snippet = "new " + typeComponent.getQualifiedName() + " []{";

            snippet += IntStream.range(0, random.nextInt(maxArraySize))
                    .mapToObj(i -> createValue(typeComponent))
                    .map(value -> value.toString())
                    .collect(Collectors.joining(","))
                    + "}";
        } else {
            type.getActualClass().getConstructor(new Class[]{});
            snippet = "new " + type.getQualifiedName() + "()";
        }
        if(snippet != null) {
            CtExpression expression = factory.Code().createCodeSnippetExpression(snippet);
            expression.setType(type);

            return expression;
        }
        } catch (Exception e) {
//                e.printStackTrace();
//            Log.debug("");
        }
        return null;
    }

    protected boolean isArray(CtTypeReference type) {
        return type.toString().contains("[]");
    }

    protected boolean isPrimitive(CtTypeReference type) {
        return type.unbox().isPrimitive();
    }

    protected CtLiteral createRandomPrimitive(CtTypeReference type) {
        Factory factory = type.getFactory();
        String typeName = type.unbox().getSimpleName();

        switch (typeName) {
            case "int" :
                return factory.Code().createLiteral(random.nextInt());
            case "long" :
                return factory.Code().createLiteral(random.nextLong());
            case "float" :
                return factory.Code().createLiteral(random.nextFloat());
            case "double" :
                return factory.Code().createLiteral(random.nextDouble());
            case "boolean" :
                return factory.Code().createLiteral(random.nextBoolean());
            case "short" :
                return factory.Code().createLiteral(random.nextInt(Short.MAX_VALUE));
            case "byte" :
                return factory.Code().createLiteral(random.nextInt(Byte.MAX_VALUE));
            case "char" :
                return factory.Code().createLiteral((char) ((byte)random.nextInt(Byte.MAX_VALUE)));
        }
        return null;
    }
}
