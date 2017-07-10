package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.utils.TypeUtils;
import spoon.reflect.code.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;

import java.util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/17/17
 */
public class AssertBuilder {

    private static String junitAssertClassName = "org.junit.Assert";

    static List<CtStatement> buildAssert(Factory factory, Set<String> notDeterministValues, Map<String, Object> observations) {
        return observations.keySet().stream()
                .filter(key -> !notDeterministValues.contains(key))
                .collect(ArrayList<CtStatement>::new,
                        (expressions, key) -> {
                            Object value = observations.get(key);
                            if (value == null) {
                                expressions.add(buildInvocation(factory, "assertNull",
                                        Collections.singletonList(
                                                factory.createCodeSnippetExpression(key)
                                        ))
                                );
                            } else if (value instanceof Boolean) {
                                expressions.add(
                                        buildInvocation(factory,
                                                (Boolean) value ? "assertTrue" : "assertFalse",
                                                Collections.singletonList(
                                                        factory.createCodeSnippetExpression(key)
                                                )
                                        )
                                );
                            } else if (TypeUtils.isArray(value)) {
                                expressions.add(buildAssertForArray(factory, key, value));
                            } else if (TypeUtils.isPrimitiveCollection(value)) {
                                expressions.add(buildSnippetAssertCollection(factory, key, (Collection) value));
                            } else if (TypeUtils.isPrimitiveMap(value)) {
                                expressions.add(buildSnippetAssertMap(factory, key, (Map) value));
                            } else {
                                expressions.add(buildInvocation(factory, "assertEquals",
                                        Arrays.asList(factory.createCodeSnippetExpression(key),
                                                printPrimitiveString(factory, value))));
                            }
                        },
                        ArrayList<CtStatement>::addAll);
    }

    private static CtInvocation buildInvocation(Factory factory, String methodName, List<CtExpression> arguments) {
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName(junitAssertClassName + "." + methodName);
        executableReference.setDeclaringType(factory.createCtTypeReference(org.junit.Assert.class));
        invocation.setExecutable(executableReference);
        invocation.setArguments(arguments); // TODO
        return invocation;
    }

    private static CtStatement buildAssertForArray(Factory factory, String expression, Object array) {
        Random r = new Random();
        String type = array.getClass().getCanonicalName();
        String arrayLocalVar1 = "array_" + Math.abs(r.nextInt());
        String arrayLocalVar2 = "array_" + Math.abs(r.nextInt());


        String forLoop = "\tfor(int ii = 0; ii <" + arrayLocalVar1 + ".length; ii++) {\n\t\t"
                + junitAssertClassName + ".assertEquals(" + arrayLocalVar1 + "[ii], " + arrayLocalVar2 + "[ii]);\n\t}";

        return factory.createCodeSnippetStatement(type + " " + arrayLocalVar1 + " = " + primitiveArrayToString(array) + ";\n\t"
                + type + " " + arrayLocalVar2 + " = " + "(" + type + ")" + expression + ";\n"
                + forLoop);
    }

    private static CtStatement buildSnippetAssertCollection(Factory factory, String expression, Collection value) {
        Random r = new Random();
        String type = value.getClass().getCanonicalName();
        String localVar = "collection_" + Math.abs(r.nextInt());
        String newCollection = type + " " + localVar + " = new " + type + "<Object>();\n";

        for (Object v : value) {
            newCollection += "\t" + localVar + ".add(" + printPrimitiveString(factory, v) + ");\n";
        }
        newCollection += "\t" + junitAssertClassName + ".assertEquals(" + localVar + ", " + expression + ");";

        return factory.createCodeSnippetStatement(newCollection);
    }

    private static CtStatement buildSnippetAssertMap(Factory factory, String expression, Map value) {
        Random r = new Random();
        String type = value.getClass().getCanonicalName();
        String localVar = "map_" + Math.abs(r.nextInt());
        String newCollection = type + " " + localVar + " = new " + type + "<Object, Object>();";

        Set<Map.Entry> set = value.entrySet();
        for (Map.Entry v : set) {
            newCollection += "\n\t" + localVar + ".put(" + printPrimitiveString(factory, v.getKey())
                    + ", " + printPrimitiveString(factory, v.getValue()) + ");\n";
        }
        newCollection += "\t" + junitAssertClassName + ".assertEquals(" + localVar + ", " + expression + ");";

        return factory.createCodeSnippetStatement(newCollection);
    }

    private static CtExpression printPrimitiveString(Factory factory, Object value) {
        if (value == null ||
                value instanceof Double ||
                value instanceof Float ||
                value instanceof Long ||
                value instanceof String ||
                value instanceof Character) {
            return factory.createLiteral(value);
        } else {
            return factory.createCodeSnippetExpression(value.toString());
        }
    }

    private static String primitiveArrayToString(Object array) {
        String type = array.getClass().getCanonicalName();

        String tmp;
        if (type.equals("int[]")) {
            tmp = Arrays.toString((int[]) array);
            return "new int[]{" + tmp.substring(1, tmp.length() - 1) + "}";
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
            tmp = Arrays.toString((double[]) array);
            return "new double[]{" + tmp.substring(1, tmp.length() - 1) + "}";
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
