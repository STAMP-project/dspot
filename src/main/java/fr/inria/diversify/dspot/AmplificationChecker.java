package fr.inria.diversify.dspot;

import fr.inria.diversify.util.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.SpoonClassNotFoundException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */

public class AmplificationChecker {

    public static boolean isCase(CtLiteral literal) {
        return literal.getParent(CtCase.class) != null;
    }

    public static boolean isAssert(CtStatement stmt) {
        return stmt instanceof CtInvocation && isAssert((CtInvocation) stmt);
    }

    public static boolean isAssert(CtInvocation invocation) {
        try {
            Class cl = invocation.getExecutable().getDeclaringType().getActualClass();
            String mthName = invocation.getExecutable().getSimpleName();
            return (mthName.startsWith("assert") || mthName.contains("fail"))
                    || isAssertInstance(cl);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAssertInstance(Class cl) {
        if (cl.equals(org.junit.Assert.class) || cl.equals(junit.framework.Assert.class))
            return true;
        Class superCl = cl.getSuperclass();
        return superCl != null && isAssertInstance(superCl);
    }

    public static boolean canBeAdded(CtInvocation invocation) {
        return !invocation.toString().startsWith("super(") && invocation.getParent() instanceof CtBlock;
    }

    public static boolean isArray(CtTypeReference type) {
        return type.toString().contains("[]");
    }

    public static boolean isPrimitive(CtTypeReference type) {
        try {
            return type.unbox().isPrimitive();
        } catch (SpoonClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isInAssert(CtLiteral lit) {
        return lit.getParent() instanceof CtInvocation && !AmplificationChecker.isAssert((CtInvocation) lit.getParent());
    }

    public static boolean isTest(CtMethod<?> candidate) {
        CtClass<?> parent = candidate.getParent(CtClass.class);
        if (candidate.getAnnotation(org.junit.Ignore.class) != null) {
            return false;
        }
        if (candidate.isImplicit()
                || candidate.getVisibility() == null
                || !candidate.getVisibility().equals(ModifierKind.PUBLIC)
                || candidate.getBody() == null
                || candidate.getBody().getStatements().size() == 0) {
            return false;
        }
        return candidate.getAnnotation(org.junit.Test.class) != null ||
                ((candidate.getSimpleName().contains("test") ||
                candidate.getSimpleName().contains("should")) && !isTestJUnit4(parent));
    }

    private static boolean isTestJUnit4(CtClass<?> classTest) {
        return classTest.getMethods().stream()
                .anyMatch(ctMethod ->
                        ctMethod.getAnnotation(org.junit.Test.class) != null
                );
    }

    public static boolean isTest(CtMethod candidate, String relativePath) {
        try {
            if (candidate.getPosition() != null
                    && candidate.getPosition().getFile() != null
                    && !candidate.getPosition().getFile().toString().contains(relativePath)) {
                return false;
            }
        } catch (Exception e) {
            Log.warn("Error during check of position of " + candidate.getSimpleName());
            return false;
        }
        return isTest(candidate);
    }
}
