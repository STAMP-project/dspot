package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.utils.RandomHelper;
import eu.stamp_project.utils.TypeUtils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtNamedElement;
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
public class AssertBuilder {

    public static final int MAX_NUMBER_OF_CHECKED_ELEMENT_IN_LIST = 5;
    private final static String JUNIT_ASSERT_CLASS_NAME = "org.junit.Assert";

    private static final Predicate<Object> isFloating = value ->
            value instanceof Double || value.getClass() == double.class ||
                    value instanceof Float || value.getClass() == float.class;

    static List<CtStatement> buildAssert(Factory factory,
                                         Set<String> notDeterministValues,
                                         Map<String, Object> observations,
                                         Double delta) {
        final Translator translator = new Translator(factory);
        return observations.keySet().stream()
                .filter(key -> !notDeterministValues.contains(key)) // TODO we may want to generate assertion on not deterministic values when it is floats
                .collect(ArrayList<CtStatement>::new,
                        (expressions, key) -> {
                            Object value = observations.get(key);
                            final CtExpression variableRead = translator.translate(key);
                            if (value == null) {
                                expressions.add(AssertGeneratorHelper.buildInvocation(factory, "assertNull",
                                        Collections.singletonList(variableRead))
                                );
                                variableRead.setType(factory.Type().NULL_TYPE);
                            } else {
                                if (value instanceof Boolean) {
                                    expressions.add(
                                            AssertGeneratorHelper.buildInvocation(factory,
                                                    (Boolean) value ? "assertTrue" : "assertFalse",
                                                    Collections.singletonList(variableRead)
                                            )
                                    );
                                } else if (TypeUtils.isArray(value)) {//TODO
                                    expressions.add(buildAssertForArray(factory, key, value));
                                } else if (TypeUtils.isPrimitiveCollection(value)) {
                                    Collection valueCollection = (Collection) value;
                                    if (valueCollection.isEmpty()) {
                                        final CtInvocation<?> isEmpty = factory.createInvocation(variableRead,
                                                factory.Type().get(Collection.class).getMethodsByName("isEmpty").get(0).getReference()
                                        );
                                        expressions.add(AssertGeneratorHelper.buildInvocation(factory, "assertTrue",
                                                Collections.singletonList(isEmpty))
                                        );
                                    } else {
                                        expressions.addAll(buildSnippetAssertCollection(factory, key, (Collection) value));
                                    }
                                } else if (TypeUtils.isPrimitiveMap(value)) {//TODO
                                    Map valueCollection = (Map) value;
                                    if (valueCollection.isEmpty()) {
                                        final CtInvocation<?> isEmpty = factory.createInvocation(variableRead,
                                                factory.Type().get(Map.class).getMethodsByName("isEmpty").get(0).getReference()
                                        );
                                        expressions.add(AssertGeneratorHelper.buildInvocation(factory, "assertTrue",
                                                Collections.singletonList(isEmpty))
                                        );
                                    } else {
                                        expressions.addAll(buildSnippetAssertMap(factory, key, (Map) value));
                                    }
                                } else {
                                    addTypeCastIfNeeded(variableRead, value);
                                    if (isFloating.test(value)) {
                                        expressions.add(AssertGeneratorHelper.buildInvocation(factory, "assertEquals",
                                                Arrays.asList(
                                                        printPrimitiveString(factory, value),
                                                        variableRead,
                                                        factory.createLiteral(delta)
                                                )));
                                    } else {
                                        if (value instanceof String) {
                                            if (!AssertGeneratorHelper.containsObjectReferences((String) value)) {
                                                expressions.add(AssertGeneratorHelper.buildInvocation(factory, "assertEquals",
                                                        Arrays.asList(printPrimitiveString(factory, value),
                                                                variableRead)));
                                            }
                                        } else {
                                            expressions.add(AssertGeneratorHelper.buildInvocation(factory, "assertEquals",
                                                    Arrays.asList(printPrimitiveString(factory, value),
                                                            variableRead)));
                                        }
                                    }
                                }
                                variableRead.setType(factory.Type().createReference(value.getClass()));
                            }
                        },
                        ArrayList<CtStatement>::addAll);
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

    private static CtStatement buildAssertForArray(Factory factory, String expression, Object array) {
        String type = array.getClass().getCanonicalName();
        String arrayLocalVar1 = "array_" + Math.abs(RandomHelper.getRandom().nextInt());
        String arrayLocalVar2 = "array_" + Math.abs(RandomHelper.getRandom().nextInt());


        String forLoop = "\tfor(int ii = 0; ii <" + arrayLocalVar1 + ".length; ii++) {\n\t\t"
                + JUNIT_ASSERT_CLASS_NAME + ".assertEquals(" + arrayLocalVar1 + "[ii], " + arrayLocalVar2 + "[ii]);\n\t}";

        return factory.createCodeSnippetStatement(type + " " + arrayLocalVar1 + " = " + primitiveArrayToString(array, factory) + ";\n\t"
                + type + " " + arrayLocalVar2 + " = " + "(" + type + ")" + expression + ";\n"
                + forLoop);
    }

    // TODO we need maybe limit assertion on a limited number of elements
    @SuppressWarnings("unchecked")
    private static List<CtStatement> buildSnippetAssertCollection(Factory factory, String expression, Collection value) {
        final CtVariableAccess variableRead = factory.createVariableRead(
                factory.createLocalVariableReference().setSimpleName(expression),
                false
        );
        final CtExecutableReference contains = factory.Type().get(Collection.class).getMethodsByName("contains").get(0).getReference();
        return (List<CtStatement>) value.stream()
                .limit(Math.min(value.size(), MAX_NUMBER_OF_CHECKED_ELEMENT_IN_LIST))
                .map(factory::createLiteral)
                .map(o ->
                        AssertGeneratorHelper.buildInvocation(factory, "assertTrue",
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
    private static List<CtStatement> buildSnippetAssertMap(Factory factory, String expression, Map value) {
        final CtVariableAccess variableRead = factory.createVariableRead(
                factory.createLocalVariableReference().setSimpleName(expression),
                false
        );
        final CtExecutableReference containsKey = factory.Type().get(Map.class).getMethodsByName("containsKey").get(0).getReference();
        final CtExecutableReference get = factory.Type().get(Map.class).getMethodsByName("get").get(0).getReference();
        return (List<CtStatement>) value.keySet().stream()
                .flatMap(key ->
                        Arrays.stream(new CtInvocation<?>[]{
                                        AssertGeneratorHelper.buildInvocation(factory, "assertTrue",
                                                Collections.singletonList(factory.createInvocation(variableRead,
                                                        containsKey, factory.createLiteral(key)
                                                        )
                                                )
                                        ),
                                        AssertGeneratorHelper.buildInvocation(factory, "assertEquals",
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
     * @param value   value to check
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

    private static String primitiveArrayToString(Object array, Factory factory) {
        String type = array.getClass().getCanonicalName();

        String tmp;
        if (type.equals("int[]")) {
            final String elements = Arrays.stream((int[]) array)
                    .boxed()
                    .map(value -> getFieldReadOrLiteral(factory, value))
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            return "new int[]{" + elements + "}";
        }
        if (type.equals("short[]")) {
            tmp = Arrays.toString((short[]) array);
            return "new short[]{" + tmp.substring(1, tmp.length() - 1) + "}";
        }
        if (type.equals("byte[]")) {
            tmp = Arrays.toString((byte[]) array);
            return "new byte[]{" + tmp.substring(1, tmp.length() - 1) + "}";
        }
        if (type.equals("long[]")) {
            tmp = Arrays.toString((long[]) array);
            return "new long[]{" + tmp.substring(1, tmp.length() - 1) + "}";
        }
        if (type.equals("float[]")) {
            tmp = Arrays.toString((float[]) array);
            return "new float[]{" + tmp.substring(1, tmp.length() - 1) + "}";
        }
        if (type.equals("double[]")) {
            final String elements = Arrays.stream((double[]) array)
                    .boxed()
                    .map(value -> getFieldReadOrLiteral(factory, value))
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            return "new double[]{" + elements + "}";
        }
        if (type.equals("boolean[]")) {
            tmp = Arrays.toString((boolean[]) array);
            return "new boolean[]{" + tmp.substring(1, tmp.length() - 1) + "}";
        }
        if (type.equals("char[]")) {
            char[] arrayChar = (char[]) array;

            if (arrayChar.length == 0) {
                return "new char[]{}";
            }
            if (arrayChar.length == 1) {
                return "new char[]{\'" + arrayChar[0] + "\'}";
            } else {
                String ret = "new char[]{\'" + arrayChar[0];
                for (int i = 1; i < arrayChar.length - 1; i++) {
                    ret += "\',\'" + arrayChar[i];
                }
                return ret + "\'}";
            }
        }

        return null;
    }

}
