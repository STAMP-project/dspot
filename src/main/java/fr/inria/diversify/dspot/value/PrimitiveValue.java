package fr.inria.diversify.dspot.value;

import spoon.reflect.code.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: Simon
 * Date: 28/09/16
 * Time: 14:28
 */
public class PrimitiveValue extends Value {
    protected String value;

    public PrimitiveValue(ValueType type, String value) {
        this.type = type;
        if(type.getType().contains("String")) {
            this.value = value.substring(0, Math.min(value.length(), 10000));
        } else {
            this.value = value;
        }
    }

    @Override
    public String getDynamicType() {
        return type.getType();
    }

    @Override
    public CtTypeReference getType() {
        return null;
    }


    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public CtExpression getValue() {
        return createNewLiteral(type.getType(), value);
    }

    public void initLocalVar(CtBlock body, CtLocalVariable localVar) {
        Factory factory = localVar.getFactory();

        if(type.getType().endsWith("[]")) {
            localVar.setDefaultExpression(factory.Code().createCodeSnippetExpression(formatValueArray()));
        }
        else if(value.startsWith("[") || value.startsWith("{")) {
            String dynamicType;
            if(type.getType().contains("<null")) {
                int index = type.getType().indexOf("<");
                dynamicType = type.getType().substring(0, index);
            } else {
                dynamicType = type.getType();
            }
            localVar.setDefaultExpression(factory.Code().createCodeSnippetExpression("new " + dynamicType +"()"));
            List<CtStatement> statements = new ArrayList<>();
            if(isCollection(dynamicType)) {
                statements = generateCollectionAddStatement(value, type.getType(), localVar.getSimpleName());
            }
            else if (isMap(dynamicType)) {
                statements = generateMapPutStatement(value, type.getType(), localVar.getSimpleName());
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
            localVar.setDefaultExpression(createNewLiteral(type.getType(), value));
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
                        return type.getSpoonFactory().Code().createCodeSnippetStatement(localVarName + ".put("
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
                    .map(value -> type.getSpoonFactory().Code().createCodeSnippetStatement(localVarName + ".add("
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

    protected String formatValueArray() {
        String valueString = "";
        if(value.length() > 2 ) {
            valueString = value.substring(1, value.length() - 1);
            if (type.getType().startsWith("char")) {
                valueString = Stream.of(valueString.split(","))
                        .map(c -> c.trim())
                        .map(c -> {
                            if((c+ "").length() == 0) {
                                return ' ';
                            } else {
                                return c;
                            }
                        })
                        .map(c -> "\'" + c + "\'")
                        .collect(Collectors.joining(","));
            }
            if (type.getType().startsWith("String")) {
                valueString = Stream.of(valueString.split(","))
                        .map(c -> "\"" + c + "\"")
                        .collect(Collectors.joining(","));
            }
        }
        return "{" + valueString  + "}";
    }

    public String simplePrintValue() {
        return createNewLiteral(type.getType(), value).toString();
    }

    protected CtLiteral createNewLiteral(String typeName, String value)  {
        Factory factory = type.getSpoonFactory();
        if(typeName.equals("null")) {
            return factory.Code().createLiteral(null);
        }
        Class<?> type = null;
        try {
            type = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
        }

        if(type == String.class) {
            return factory.Code().createLiteral(value);
        }
        if(typeName.equals("int") || type == Integer.class) {
            try {
                return factory.Code().createLiteral(Integer.parseInt(value));

            }  catch (Exception e) {
                e.printStackTrace();
                return factory.Code().createLiteral(0);
            }
        }
        if(typeName.equals("boolean") || type == Boolean.class) {
            return factory.Code().createLiteral(Boolean.parseBoolean(value));
        }
        if(typeName.equals("char") || type == Character.class) {
            return factory.Code().createLiteral(value.charAt(0));
        }
        if(typeName.equals("byte") || type == Byte.class) {
            return factory.Code().createLiteral(Byte.parseByte(value));
        }
        if(typeName.equals("short") || type == Short.class) {
            return factory.Code().createLiteral(Short.parseShort(value));
        }
        if(typeName.equals("double") || type == Double.class) {
            return factory.Code().createLiteral(Double.parseDouble(value));
        }
        if(typeName.equals("long") || type == Long.class) {
            return factory.Code().createLiteral(Long.parseLong(value));
        }
        if(typeName.equals("float") || type == Float.class) {
            return factory.Code().createLiteral(Float.parseFloat(value));
        }

        return factory.Code().createLiteral(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveValue value1 = (PrimitiveValue) o;

        return value != null ? value.equals(value1.value) : value1.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
