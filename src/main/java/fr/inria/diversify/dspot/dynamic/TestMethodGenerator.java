package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.dspot.value.MethodCall;
import fr.inria.diversify.dspot.value.Value;
import fr.inria.diversify.dspot.value.ValueFactory;
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
    protected ValueFactory valueFactory;
    protected static Map<CtType, Integer> testCount = new HashMap<>();
    protected Factory factory;
    protected final static Set<ModifierKind> testModifier = new HashSet<>();
    protected final CtTypeReference  voidType;
    protected final static List<CtParameter<?>> emptyParametersList = new ArrayList<CtParameter<?>>(0);
    static {
        testModifier.add(ModifierKind.PUBLIC);
    }

    public TestMethodGenerator(Factory factory, ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        this.factory = factory;
        voidType = factory.Type().VOID_PRIMITIVE;
    }

    public boolean generateTestFromBody(MethodCall methodCall, CtType testClass) {
        CtBlock body = cloneAndRemoveReturn(methodCall.getMethod().getBody());
        addParametersAsLocalVar(methodCall, body);

        CtMethod newTest = factory.Method().create((CtClass<?>)testClass,
                testModifier,
                voidType,
                "test_" + methodCall.getMethod().getSimpleName() + testId(testClass),
                emptyParametersList,
                new HashSet<CtTypeReference<? extends Throwable>>(),
                body);

        newTest.addAnnotation(testAnnotation());
        return true;
    }
    public boolean generateTestFromInvocation(MethodCall methodCall, CtType testClass) {
        CtBlock body = factory.Core().createBlock();
        List<CtLocalVariable> localVariables = addParametersAsLocalVar(methodCall, body);

        CtExpression target = null;
        if(methodCall.getTarget() != null) {
            CtExpression constructorCall = valueFactory.findConstructorCall(methodCall.getTarget());
            if (constructorCall != null) {
                CtLocalVariable targetCreation = factory.Code().createLocalVariable(factory.Type().createReference(methodCall.getTarget()),
                        "target", constructorCall);
                body.addStatement(targetCreation);
                target = factory.Code().createVariableRead((CtLocalVariableReference) factory.Code().createLocalVariableReference(targetCreation), false);
            }
        }  else {
            target = factory.Code().createTypeAccess(methodCall.getMethod().getDeclaringType().getReference());
        }

        if(target != null) {
            List<CtExpression<?>> localVarRefs = (List<CtExpression<?>>) localVariables.stream()
                    .map(var -> factory.Code().createLocalVariableReference(var))
                    .map(varRef -> factory.Code().createVariableRead((CtLocalVariableReference) varRef, false))
                    .map(varRef -> (CtExpression<?>) varRef)
                    .collect(Collectors.toList());

            CtInvocation call = factory.Code().createInvocation(target,
                    factory.Executable().createReference(methodCall.getMethod()),
                    localVarRefs);

            body.addStatement(call);

            CtMethod newTest = factory.Method().create((CtClass<?>) testClass,
                    testModifier,
                    voidType,
                    "test_" + methodCall.getMethod().getSimpleName() + testId(testClass),
                    emptyParametersList,
                    methodCall.getMethod().getThrownTypes(),
                    body);

            newTest.addAnnotation(testAnnotation());
            return true;
        }
        return false;
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

    protected List<CtLocalVariable> addParametersAsLocalVar(MethodCall methodCall, CtBlock body) {
        List<Value> parameterValues = methodCall.getParameterValues();
        List<CtParameter> parameters = methodCall.getMethod().getParameters();
        List<CtLocalVariable> localVariables = new ArrayList<>(parameterValues.size());

        for(int i = 0; i < parameterValues.size(); i++) {
            CtLocalVariable localVar = null;
            try {
                CtParameter parameter = parameters.get(i);
                Value value = parameterValues.get(i);
                localVar = factory.Code().createLocalVariable(
                        generateStaticType(parameter.getType(),value.getDynamicType()),
                        parameter.getSimpleName(),
                        null);
                body.getStatements().add(0, localVar);
                localVar.setParent(body);
                localVariables.add(localVar);

                parameterValues.get(i).initLocalVar(body, localVar);
            } catch (Exception e) {
                localVar.setAssignment(factory.Code().createLiteral(null));
            }
        }
        return localVariables;
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

    protected CtBlock cloneAndRemoveReturn(CtBlock body) {
        CtBlock bodyWithoutReturn = factory.Core().clone(body);
        List<CtReturn> returns = Query.getElements(bodyWithoutReturn, new TypeFilter(CtReturn.class));

        returns.stream()
                .peek(ret -> {
                    CtElement parent = ret.getParent();
                    if(ret.getParent() instanceof CtIf) {
                        CtIf ifStmt = (CtIf) parent;
                        if (ifStmt.getThenStatement().equals(ret)) {
                            ifStmt.setThenStatement(factory.Core().createBlock());
                        } else {
                            ifStmt.setElseStatement(factory.Core().createBlock());
                        }
                    } else if(ret.getParent() instanceof CtLoop) {
                        CtLoop loopStmt = (CtLoop) parent;
                        loopStmt.setBody(factory.Core().createBlock());
                    } else {
                        ret.getParent(CtStatementList.class).removeStatement(ret);
                    }
                })
                .filter(ret -> ret.getReturnedExpression() != null)
                .forEach(ret -> {
                    List<CtInvocation> invocations = Query.getElements(ret, new TypeFilter(CtInvocation.class));
                    invocations.stream()
                            .forEach(invocation -> ret.getParent(CtStatementList.class).addStatement(invocation));
                });
        return bodyWithoutReturn;
    }

    protected Integer testId(CtType declaringClass) {
        if(!testCount.containsKey(declaringClass)) {
            testCount.put(declaringClass, 0);
        }
        return testCount.put(declaringClass, testCount.get(declaringClass) +1);
    }
}
