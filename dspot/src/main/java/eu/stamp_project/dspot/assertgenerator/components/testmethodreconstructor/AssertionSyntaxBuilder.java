package eu.stamp_project.dspot.assertgenerator.components.testmethodreconstructor;

import eu.stamp_project.dspot.assertgenerator.components.testmethodreconstructor.assertionsyntaxbuilder.AggregateTypeBuilder;
import eu.stamp_project.dspot.assertgenerator.components.utils.AssertionGeneratorUtils;
import eu.stamp_project.dspot.assertgenerator.components.testmethodreconstructor.assertionsyntaxbuilder.Translator;
import eu.stamp_project.test_framework.assertions.AssertEnum;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.TypeUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/17/17
 */
public class AssertionSyntaxBuilder {

    public static final int MAX_NUMBER_OF_CHECKED_ELEMENT_IN_LIST = 5;

    private static final Predicate<Object> isFloating = value ->
            value instanceof Double || value.getClass() == double.class ||
                    value instanceof Float || value.getClass() == float.class;

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
                        addOtherType(invocations,testMethod,value,variableRead,factory,delta);
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

    private static void addOtherType(List<CtStatement> invocations,CtMethod<?> testMethod,
                                        Object value,CtExpression variableRead,Factory factory,Double delta) {
        addTypeCastIfNeeded(variableRead, value);
        if (isFloating.test(value)) {
            invocations.add(
                    TestFramework.get().buildInvocationToAssertion(testMethod, AssertEnum.ASSERT_EQUALS,
                            Arrays.asList(
                                    printPrimitiveString(factory, value),
                                    variableRead,
                                    factory.createLiteral(delta)
                            )));
        } else {
            if (value instanceof String) {

                // String
                addString(invocations,testMethod,value,variableRead,factory);
            } else {
                invocations.add(TestFramework.get().buildInvocationToAssertion(testMethod, AssertEnum.ASSERT_EQUALS,
                        Arrays.asList(printPrimitiveString(factory, value),
                                variableRead)));
            }
        }
    }

    private static void addString(List<CtStatement> invocations,CtMethod<?> testMethod,
                                     Object value,CtExpression variableRead,Factory factory) {
        if (AssertionGeneratorUtils.canGenerateAnAssertionFor((String) value)) {
            invocations.add(TestFramework.get().buildInvocationToAssertion(testMethod, AssertEnum.ASSERT_EQUALS,
                    Arrays.asList(printPrimitiveString(factory, value),
                            variableRead)));
        }
    }

    private static void addTypeCastIfNeeded(CtExpression<?> variableRead, Object value) {
        if (value instanceof Short) {
            variableRead.addTypeCast(variableRead.getFactory().Type().shortPrimitiveType());
        } else if (value instanceof Integer) {
            variableRead.addTypeCast(variableRead.getFactory().Type().integerPrimitiveType());
        } else if (value instanceof Long) {
            variableRead.addTypeCast(variableRead.getFactory().Type().longPrimitiveType());
        } else if (value instanceof Byte) {
            variableRead.addTypeCast(variableRead.getFactory().Type().bytePrimitiveType());
        } else if (value instanceof Float) {
            variableRead.addTypeCast(variableRead.getFactory().Type().floatPrimitiveType());
        } else if (value instanceof Double) {
            variableRead.addTypeCast(variableRead.getFactory().Type().doublePrimitiveType());
        } else if (value instanceof Character) {
            variableRead.addTypeCast(variableRead.getFactory().Type().characterPrimitiveType());
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

    private static CtExpression printPrimitiveString(Factory factory, Object value) {
        if (value instanceof String ||
                value instanceof Short ||
                value.getClass() == short.class ||
                value instanceof Double ||
                value.getClass() == double.class ||
                value instanceof Float ||
                value.getClass() == float.class ||
                value instanceof Long ||
                value.getClass() == long.class ||
                value instanceof Character ||
                value.getClass() == char.class ||
                value instanceof Byte ||
                value.getClass() == byte.class ||
                value instanceof Integer ||
                value.getClass() == int.class) {
            return getFieldReadOrLiteral(factory, value);
        } else {
            return factory.createCodeSnippetExpression(value.toString());
        }
    }

    private static CtExpression getFieldReadOrLiteral(Factory factory, Object value) {
        if (isAFieldRead(value, factory)) {
            return getCtFieldRead(value, factory);
        } else {
            return factory.createLiteral(value);
        }
    }

    private static CtFieldRead getCtFieldRead(Object value, Factory factory) {
        final CtFieldRead fieldRead = factory.createFieldRead();
        final CtClass<?> doubleClass = factory.Class().get(value.getClass());
        final CtField<?> field = doubleClass.getField(getRightField(value, factory));
        final CtFieldReference<?> reference = field.getReference();
        fieldRead.setVariable(reference);
        return fieldRead;
    }

    private static final Class<?>[] supportedClassesForFieldRead = new Class[]{Integer.class, Double.class};

    private static String getRightField(Object value, Factory factory) {
        return Arrays.stream(supportedClassesForFieldRead).map(aClass ->
                factory.Class().get(aClass)
                        .getFields()
                        .stream()
                        .filter(CtModifiable::isStatic)
                        .filter(CtModifiable::isFinal)
                        .filter(ctField -> {
                            try {
                                return value.equals(aClass.getField(ctField.getSimpleName()).get(null));
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                        .findFirst()
                        .map(CtNamedElement::getSimpleName)
                        .orElse("")
        ).filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(value.toString());
    }

    /**
     * This method checks if the given value is a field. To do this, it uses the classes in <code>supportedClassesForFieldRead</code>
     * and reflection
     *
     * @param value   value to checkEnum
     * @param factory factory with spoon model
     * @return true if the value is a field read, false otherwise
     */
    private static boolean isAFieldRead(Object value, Factory factory) {
        return (!Pattern.compile("\\d*").matcher(value.toString()).matches()) &&
                Arrays.stream(supportedClassesForFieldRead).anyMatch(aClass ->
                        factory.Class().get(aClass)
                                .getFields()
                                .stream()
                                .filter(CtModifiable::isStatic)
                                .filter(CtModifiable::isFinal)
                                .anyMatch(ctField -> {
                                    try {
                                        return value.equals(aClass.getField(ctField.getSimpleName()).get(null));
                                    } catch (Exception ignored) {
                                        return false;
                                    }
                                }));
    }
}
