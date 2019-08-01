package eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.testmethodreconstructor_components;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.testmethodreconstructor_components.assertionsyntaxbuilder_components.AggregateTypeBuilder;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.testmethodreconstructor_components.assertionsyntaxbuilder_components.InvocationSetup;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.testmethodreconstructor_components.assertionsyntaxbuilder_components.Translator;
import eu.stamp_project.test_framework.assertions.AssertEnum;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.TypeUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/17/17
 */
public class AssertionSyntaxBuilder {

    public static final int MAX_NUMBER_OF_CHECKED_ELEMENT_IN_LIST = 5;

    /**
     *
     * @param testMethod
     * @param notDeterministValues
     * @param observations
     * @param delta
     * @return
     */
    public static List<CtStatement> buildAssert(CtMethod<?> testMethod,
                                                Set<String> notDeterministValues,
                                                Map<String, Object> observations,
                                                Double delta) {
        final Factory factory = InputConfiguration.get().getFactory();
        final List<CtStatement> invocations = new ArrayList<>();
        for (String observationKey : observations.keySet()) {
            if (!notDeterministValues.contains(observationKey)) {
                Object value = observations.get(observationKey);
                final CtExpression variableRead = translate(observationKey,factory);
                if (value == null) {
                    nullValue(invocations,testMethod,variableRead,factory);
                } else {

                    // Boolean
                    if (value instanceof Boolean) {
                        addBoolean(invocations,testMethod,value,variableRead);
                    } else if (TypeUtils.isPrimitiveCollection(value)) {

                        // Primitive collection
                        addPrimitiveCollection(invocations,testMethod,value,variableRead,factory,observationKey);
                    } else if (TypeUtils.isArray(value)) {

                        // Array
                        addArray(invocations,testMethod,value,variableRead,factory);
                    } else if (TypeUtils.isPrimitiveMap(value)) {//TODO

                        // Primitive map
                        addPrimitiveMap(invocations,testMethod,value,variableRead,factory,observationKey);
                    } else {

                        // Other types
                        InvocationSetup.addRemainingType(invocations,testMethod,value,variableRead,factory,delta);
                    }
                    variableRead.setType(factory.Type().createReference(value.getClass()));
                }
            }
        }
        return invocations;
    }

    private static CtExpression translate(String observationKey,Factory factory) {
        final Translator translator = new Translator(factory);
        if (observationKey.contains("[")) {
            return factory.createCodeSnippetExpression(observationKey);
        } else {
            return translator.translate(observationKey);
        }
    }

    private static void nullValue(List<CtStatement> invocations,CtMethod<?> testMethod,
                                  CtExpression variableRead,Factory factory) {
        final CtInvocation<?> assertNull = TestFramework.get()
                .buildInvocationToAssertion(testMethod, AssertEnum.ASSERT_NULL, Collections.singletonList(variableRead));
        invocations.add(assertNull);
        variableRead.setType(factory.Type().NULL_TYPE);
    }

    private static void addBoolean(List<CtStatement> invocations,CtMethod<?> testMethod,
                                   Object value,CtExpression variableRead){
        invocations.add(
                TestFramework.get()
                        .buildInvocationToAssertion(testMethod,
                                (Boolean) value ? AssertEnum.ASSERT_TRUE : AssertEnum.ASSERT_FALSE,
                                Collections.singletonList(variableRead)
                        )
        );
    }

    private static void addPrimitiveCollection(List<CtStatement> invocations,CtMethod<?> testMethod,
                                               Object value,CtExpression variableRead,Factory factory,String observationKey) {
        Collection valueCollection = (Collection) value;
        if (valueCollection.isEmpty()) {
            final CtInvocation<?> isEmpty = factory.createInvocation(variableRead,
                    factory.Type().get(Collection.class).getMethodsByName("isEmpty").get(0).getReference()
            );
            invocations.add(
                    TestFramework.get().buildInvocationToAssertion(testMethod, AssertEnum.ASSERT_TRUE,
                            Collections.singletonList(isEmpty)
                    )
            );
        } else {
            invocations.addAll(buildSnippetAssertCollection(factory, testMethod, observationKey, (Collection) value));
        }
    }

    private static void addArray(List<CtStatement> invocations,CtMethod<?> testMethod,
                                 Object value,CtExpression variableRead,Factory factory) {
        if (AggregateTypeBuilder.isPrimitiveArray(value)) {
            CtExpression expectedValue = factory.createCodeSnippetExpression(AggregateTypeBuilder.getNewArrayExpression(value));
            List<CtExpression> list;
            if (AggregateTypeBuilder.getArrayComponentType(value).equals("float")) {
                list = Arrays.asList(expectedValue, variableRead, factory.createLiteral(0.1F));
            } else if (AggregateTypeBuilder.getArrayComponentType(value).equals("double")) {
                list = Arrays.asList(expectedValue, variableRead, factory.createLiteral(0.1));
            } else {
                list = Arrays.asList(expectedValue, variableRead);
            }
            invocations.add(TestFramework.get().buildInvocationToAssertion(testMethod,
                    AssertEnum.ASSERT_ARRAY_EQUALS, list));
        }
    }

    private static void addPrimitiveMap(List<CtStatement> invocations,CtMethod<?> testMethod,
                                        Object value,CtExpression variableRead,Factory factory,String observationKey) {
        Map valueCollection = (Map) value;
        if (valueCollection.isEmpty()) {
            final CtInvocation<?> isEmpty = factory.createInvocation(variableRead,
                    factory.Type().get(Map.class).getMethodsByName("isEmpty").get(0).getReference()
            );
            invocations.add(TestFramework.get().buildInvocationToAssertion(
                    testMethod,
                    AssertEnum.ASSERT_TRUE,
                    Collections.singletonList(isEmpty)
                    )
            );
        } else {
            invocations.addAll(buildSnippetAssertMap(factory, testMethod, observationKey, (Map) value));
        }
    }

    // TODO we need maybe limit assertion on a limited number of elements
    @SuppressWarnings("unchecked")
    private static List<CtInvocation<?>> buildSnippetAssertCollection(Factory factory, CtMethod<?> testMethod, String expression, Collection value) {
        final CtVariableAccess variableRead = factory.createVariableRead(
                factory.createLocalVariableReference().setSimpleName(expression),
                false
        );
        final CtExecutableReference contains = factory.Type().get(Collection.class).getMethodsByName("contains").get(0).getReference();
        return (List<CtInvocation<?>>) value.stream()
                .limit(Math.min(value.size(), MAX_NUMBER_OF_CHECKED_ELEMENT_IN_LIST))
                .map(factory::createLiteral)
                .map(o ->
                        TestFramework.get().buildInvocationToAssertion(
                                testMethod, AssertEnum.ASSERT_TRUE,
                                Collections.singletonList(factory.createInvocation(variableRead,
                                        contains, (CtLiteral) o
                                        )
                                )
                        )
                )
                .collect(Collectors.toList());
    }

    // TODO we need maybe limit assertion on a limited number of elements
    @SuppressWarnings("unchecked")
    private static List<CtInvocation<?>> buildSnippetAssertMap(Factory factory, CtMethod<?> testMethod, String expression, Map value) {
        final CtVariableAccess variableRead = factory.createVariableRead(
                factory.createLocalVariableReference().setSimpleName(expression),
                false
        );
        final CtExecutableReference containsKey = factory.Type().get(Map.class).getMethodsByName("containsKey").get(0).getReference();
        final CtExecutableReference get = factory.Type().get(Map.class).getMethodsByName("get").get(0).getReference();
        return (List<CtInvocation<?>>) value.keySet().stream()
                .flatMap(key ->
                        Arrays.stream(new CtInvocation<?>[]{
                                        TestFramework.get().buildInvocationToAssertion(testMethod, AssertEnum.ASSERT_TRUE,
                                                Collections.singletonList(factory.createInvocation(variableRead,
                                                        containsKey, factory.createLiteral(key)
                                                        )
                                                )
                                        ),
                                        TestFramework.get().buildInvocationToAssertion(testMethod, AssertEnum.ASSERT_EQUALS,
                                                Arrays.asList(factory.createLiteral(value.get(key)),
                                                        factory.createInvocation(variableRead,
                                                                get, factory.createLiteral(key))
                                                )
                                        )
                                }
                        )
                ).collect(Collectors.toList());
    }
}
