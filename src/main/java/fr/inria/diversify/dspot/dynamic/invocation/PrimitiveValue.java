package fr.inria.diversify.dspot.dynamic.invocation;

import fr.inria.diversify.dspot.dynamic.objectInstanciationTree.ObjectInstantiationBuilder;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.factory.Factory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 28/09/16
 * Time: 14:28
 */
public class PrimitiveValue implements Value {
    protected String dynamicType;
    protected String value;
    protected Factory factory;

    public PrimitiveValue(String dynamicType, String value, Factory factory) {
        this.dynamicType = dynamicType;
        this.value = value;
        this.factory = factory;
    }

    @Override
    public String getDynamicType() {
        return dynamicType;
    }

    public void initLocalVar(CtLocalVariable localVar, ObjectInstantiationBuilder objectInstantiationBuilder) {
        CtBlock body = localVar.getParent(CtBlock.class);

        if(value.startsWith("[") || value.startsWith("{")) {
            String type;
            if(dynamicType.contains("<null")) {
                int index = dynamicType.indexOf("<");
                type = dynamicType.substring(0, index);
            } else {
                type = dynamicType;
            }
            localVar.setDefaultExpression(factory.Code().createCodeSnippetExpression("new " + type +"()"));
            List<CtStatement> statements = new ArrayList<>();
            if(isCollection(type)) {
                statements = generateCollectionAddStatement(value, dynamicType, localVar.getSimpleName());
            }
            else if (isMap(type)) {
                statements = generateMapPutStatement(value, dynamicType, localVar.getSimpleName());
            }
            else {
                if(dynamicType.startsWith("byte")) {
                    localVar.setDefaultExpression(factory.Code().createCodeSnippetExpression("\"" + value +"\".getBytes()"));
                }
            }

            int count = 1;
            for (CtStatement addStmt : statements) {
                body.getStatements().add(count, addStmt);
                count++;
            }
        } else {
            localVar.setDefaultExpression(createNewLiteral(dynamicType, value));
        }
    }

    protected List<CtStatement> generateMapPutStatement(String values, String dynamicTypeName, String localVarName) {
        String mapValues = values.substring(1, values.length() - 1);
        if(mapValues.length() != 0) {
            int index = dynamicTypeName.indexOf(",");
            String keyGenericType = dynamicTypeName.substring(dynamicTypeName.indexOf("<") + 1, index );
            String valueGenericType = dynamicTypeName.substring(index + 2, dynamicTypeName.length() - 1);

            return Arrays.stream(mapValues.split(", "))
                    .map(value -> {
                        String[] split = value.split("=");
                        return factory.Code().createCodeSnippetStatement(localVarName + ".put("
                                + createNewLiteral(keyGenericType, split[0]) + ", "
                                + createNewLiteral(valueGenericType, split[1]) +")");
                    })
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    protected List<CtStatement> generateCollectionAddStatement(String values, String dynamicTypeName, String localVarName) {
        String collectionValues = values.substring(1, values.length() - 1);
        if(collectionValues.length() != 0 ) {
            String collectionGenericType = dynamicTypeName.substring(dynamicTypeName.indexOf("<") + 1, dynamicTypeName.length() - 1);

            return Arrays.stream(collectionValues.split(", "))
                    .map(value -> factory.Code().createCodeSnippetStatement(localVarName + ".add("
                            + createNewLiteral(collectionGenericType, value) + ")"))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    protected boolean isMap(String className) {
        try {
            return Map.class.isAssignableFrom(Class.forName(removeGeneric(className)));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected boolean isCollection(String className) {
        try {
            return Collection.class.isAssignableFrom(Class.forName(removeGeneric(className)));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected String removeGeneric(String className) {
        if(className.contains("<")) {
            int index = className.indexOf("<");
            return className.substring(0, index);
        } else {
            return className;
        }
    }

    public String simplePrintValue() {
        return createNewLiteral(dynamicType, value).toString();
    }

    protected CtLiteral createNewLiteral(String typeName, String value)  {
        if(typeName.equals("null")) {
            return factory.Code().createLiteral(null);
        }
        Class<?> type = null;
        try {
            type = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(type == Boolean.class) {
            return factory.Code().createLiteral(Boolean.parseBoolean(value));
        }
        if(type == Character.class) {
            return factory.Code().createLiteral(value.charAt(0));
        }
        if(type == Byte.class) {
            return factory.Code().createLiteral(Byte.parseByte(value));
        }
        if(type == Short.class) {
            return factory.Code().createLiteral(Short.parseShort(value));
        }
        if(type == Integer.class) {
            return factory.Code().createLiteral(Integer.parseInt(value));
        }
        if(type == Long.class) {
            return factory.Code().createLiteral(Long.parseLong(value));
        }
        if(type == Float.class) {
            return factory.Code().createLiteral(Float.parseFloat(value));
        }
        if(type == Double.class) {
            return factory.Code().createLiteral(Double.parseDouble(value));
        }
        if(type == String.class) {
            return factory.Code().createLiteral(value);
        }

        return factory.Code().createLiteral(value);
    }


}
