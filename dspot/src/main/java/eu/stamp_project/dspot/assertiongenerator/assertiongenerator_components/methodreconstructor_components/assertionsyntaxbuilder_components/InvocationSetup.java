package eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.methodreconstructor_components.assertionsyntaxbuilder_components;

import eu.stamp_project.dspot.assertiongenerator.utils.AssertionGeneratorUtils;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.test_framework.assertions.AssertEnum;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class InvocationSetup {

    private static final Predicate<Object> isFloating = value ->
            value instanceof Double || value.getClass() == double.class ||
                    value instanceof Float || value.getClass() == float.class;

    /**
     * Setup and create assertion syntax for a type other than boolean, primitive collection, array and primitive map.
     * The new assertion syntax is put into the {@code invocations} parameter.
     *
     * @param invocations A list holding assertion syntaxes
     * @param testMethod Test Method
     * @param value Expected value of assertion
     * @param variableRead Name of variable holding actual value of assertion
     * @param factory Factory
     * @param delta Precision for floating type assertions
     */
    public static void addRemainingType(List<CtStatement> invocations, CtMethod<?> testMethod,
                                        Object value, CtExpression variableRead, Factory factory, Double delta) {
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
