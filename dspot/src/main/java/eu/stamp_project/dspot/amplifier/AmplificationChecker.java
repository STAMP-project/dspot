package eu.stamp_project.dspot.amplifier;

import spoon.SpoonException;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */

public class AmplificationChecker {

    public static boolean isCase(CtLiteral literal) {
        return literal.getParent(CtCase.class) != null;
    }

    private final static List<String> ASSERTIONS_PACKAGES =
            Arrays.asList(
                    "org.junit.Assert", // JUnit 4
                    "org.junit.jupiter.api.Assertions", // JUnit 5
                    "com.google.common.truth.*", // TODO Truth
                    "org.assertj.core.api.*", //  assertJ
                    "junit.framework.TestCase" // JUnit 3
            );

    public static boolean canBeAdded(CtInvocation invocation) {
        return !invocation.toString().startsWith("super(");// && invocation.getParent() instanceof CtBlock;
    }

    public static boolean isArray(CtTypeReference type) {
        return type instanceof CtArrayTypeReference;
    }

    public static boolean isPrimitive(CtTypeReference type) {
        try {
            return type.unbox().isPrimitive();
        } catch (SpoonException e) {
            return false;
        }
    }

}
