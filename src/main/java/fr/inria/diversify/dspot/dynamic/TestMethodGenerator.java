package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.util.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 07/04/16
 * Time: 11:00
 */
public class TestMethodGenerator {
    protected Map<CtType, Integer> testCount;
    protected int testId;
    protected Factory factory;
    protected final static Set<ModifierKind> testModifier = new HashSet<>();
    protected final CtTypeReference  voidType;
    protected final static List<CtParameter<?>> emptyParametersList = new ArrayList<CtParameter<?>>(0);
    static {
        testModifier.add(ModifierKind.PUBLIC);
    }

    public TestMethodGenerator(Factory factory) {
        this.factory = factory;
        testCount = new HashMap<>();
        voidType = factory.Type().VOID_PRIMITIVE;
    }

    public CtMethod generateTestFromBody(CtMethod method, CtType testClass, List<String> typesAndParameters) {
        CtBlock body = cloneAndRemoveReturn(method.getBody());
        addParametersAsLocalVar(body, method.getParameters(), typesAndParameters);

        CtMethod newTest = factory.Method().create((CtClass<?>)testClass,
                testModifier,
                voidType,
                "test_" + method.getSimpleName() + testId(testClass),
                emptyParametersList,
                new HashSet<CtTypeReference<? extends Throwable>>(),
                body);

        newTest.addAnnotation(testAnnotation());
        return newTest;
    }

    public CtMethod generateTestFromInvocation(CtMethod method, CtType testClass, String targetType, List<String> typesAndParameters) {
        CtBlock body = factory.Core().createBlock();
        List<CtLocalVariable> localVariables = addParametersAsLocalVar(body, method.getParameters(), typesAndParameters);

        CtExpression target = null;
        if(targetType != null) {
            if(!targetType.equals("static")) {
                CtClass type = factory.Class().get(targetType);
                CtLocalVariable targetCreation = factory.Code().createLocalVariable(factory.Type().createReference(type),
                        "target",
                        factory.Code().createConstructorCall(type.getReference()));
                body.addStatement(targetCreation);
                target = factory.Code().createVariableRead((CtLocalVariableReference) factory.Code().createLocalVariableReference(targetCreation), false);
            } else {
                target = factory.Code().createTypeAccess(method.getType());
            }
        }

        List<CtExpression<?>> localVarRefs = (List<CtExpression<?>>)localVariables.stream()
                .map(var -> factory.Code().createLocalVariableReference(var))
                .map(varRef -> factory.Code().createVariableRead((CtLocalVariableReference)varRef, false))
                .map( varRef -> (CtExpression<?>)varRef)
                .collect(Collectors.toList());

        CtInvocation call = factory.Code().createInvocation(target,
                factory.Executable().createReference(method),
                localVarRefs);

        body.addStatement(call);

        CtMethod newTest = factory.Method().create((CtClass<?>)testClass,
                testModifier,
                voidType,
                "test_i_" + method.getSimpleName() + testId(testClass),
                emptyParametersList,
                new HashSet<CtTypeReference<? extends Throwable>>(),
                body);

        newTest.addAnnotation(testAnnotation());
        return newTest;
    }

    protected CtAnnotation testAnnotation(){
        CtAnnotation annotation = factory.Core().createAnnotation();
        CtTypeReference<Object> ref = factory.Core().createTypeReference();
        ref.setSimpleName("Test");

        CtPackageReference refPackage = factory.Core().createPackageReference();
        refPackage.setSimpleName("org.junit");
        ref.setPackage(refPackage);
        annotation.setAnnotationType(ref);

        return annotation;
    }

    protected List<CtLocalVariable> addParametersAsLocalVar(CtBlock body, List<CtParameter> parameters, List<String> typesAndParameters) {
        List<CtLocalVariable> localVariables = new ArrayList<>(parameters.size()) ;

        for(int i = 0; i < parameters.size(); i++) {
            try {
                String[] split = typesAndParameters.get(i).split(":");
                CtLocalVariable localVar;
                if(split.length != 1) {
                    localVar = addLocalVar(body, parameters.get(i), split[0], split[1]);
                } else {
                    localVar = addLocalVar(body, parameters.get(i), split[0], "");
                }
                localVariables.add(localVar);
            } catch (Exception e) {
                e.printStackTrace();
                Log.debug("");
            }
        }
        return localVariables;
    }

    protected CtLocalVariable addLocalVar(CtBlock body, CtParameter parameter, String dynamicTypeName, String value) {
        CtLocalVariable localVar = factory.Code().createLocalVariable(generateStaticType(parameter.getType(),dynamicTypeName),
                parameter.getSimpleName(),
                null);
        body.getStatements().add(0, localVar);

        if(value.startsWith("[") || value.startsWith("{")) {
            String type;
            if(dynamicTypeName.contains("<null")) {
                int index = dynamicTypeName.indexOf("<");
                type = dynamicTypeName.substring(0, index);
            } else {
                type = dynamicTypeName;
            }
            localVar.setDefaultExpression(factory.Code().createCodeSnippetExpression("new " + type +"()"));
            List<CtStatement> statements = new ArrayList<>();
            if(isCollection(type)) {
                statements = generateCollectionAddStatement(value, dynamicTypeName, localVar.getSimpleName());
            }
            if(isMap(type)) {
                statements = generateMapPutStatement(value, dynamicTypeName, localVar.getSimpleName());
            }
//            if(isArray(type)) {
//
//            }
            int count = 1;
            for (CtStatement addStmt : statements) {
                body.getStatements().add(count, addStmt);
                count++;
            }
        } else {
            localVar.setDefaultExpression(createNewLiteral(dynamicTypeName, value));
        }
        return localVar;
    }

    protected CtTypeReference generateStaticType(CtTypeReference parameterType, String dynamicTypeName) {
        CtTypeReference type = factory.Core().clone(parameterType);
        type.getActualTypeArguments().clear();

        if((dynamicTypeName.contains("<") || dynamicTypeName.contains(">"))
                && !(dynamicTypeName.contains("<null") || dynamicTypeName.contains("null>"))) {

            String[] genericTypes = dynamicTypeName.substring(dynamicTypeName.indexOf("<") + 1, dynamicTypeName.length() - 1).split(", ");
            Arrays.stream(genericTypes)
                    .forEach(genericType -> type.getActualTypeArguments().add(factory.Type().createReference(genericType)));
        }
        return type;
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
//            e.printStackTrace();
            return false;
        }
    }

    protected boolean isCollection(String className) {
        try {
            return Collection.class.isAssignableFrom(Class.forName(removeGeneric(className)));
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
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

    protected CtBlock cloneAndRemoveReturn(CtBlock body) {
        CtBlock bodyWithoutReturn = factory.Core().clone(body);
        List<CtReturn> returns = Query.getElements(bodyWithoutReturn, new TypeFilter(CtReturn.class));

        returns.stream()
                .peek(ret -> bodyWithoutReturn.removeStatement(ret))
                .filter(ret -> ret.getReturnedExpression() != null)
                .forEach(ret -> {
                    List<CtInvocation> invocations = Query.getElements(ret, new TypeFilter(CtInvocation.class));
                    invocations.stream()
                            .forEach(invocation -> bodyWithoutReturn.addStatement(invocation));
                });
        return bodyWithoutReturn;
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

    protected Integer testId(CtType declaringClass) {
        if(!testCount.containsKey(declaringClass)) {
            testCount.put(declaringClass, 0);
        }
        return testCount.put(declaringClass, testCount.get(declaringClass) +1);
    }
}
