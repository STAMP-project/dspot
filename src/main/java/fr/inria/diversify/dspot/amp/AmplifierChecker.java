package fr.inria.diversify.dspot.amp;

import spoon.reflect.code.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.reference.SpoonClassNotFoundException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */

class AmplifierChecker {

    static boolean isCase(CtLiteral literal) {
        return literal.getParent(CtCase.class) != null;
    }

    static boolean isAssert(CtStatement stmt) {
        return stmt instanceof CtInvocation && isAssert((CtInvocation) stmt);
    }

    static boolean isAssert(CtInvocation invocation) {
        try {
            Class cl = invocation.getExecutable().getDeclaringType().getActualClass();
            String mthName = invocation.getExecutable().getSimpleName();
            return (mthName.startsWith("assert") || mthName.contains("fail"))
                    || isAssertInstance(cl);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isAssertInstance(Class cl) {
        if (cl.equals(org.junit.Assert.class) || cl.equals(junit.framework.Assert.class))
            return true;
        Class superCl = cl.getSuperclass();
        return superCl != null && isAssertInstance(superCl);
    }

    public static boolean canBeAdded(CtInvocation invocation) {
        return !invocation.toString().startsWith("super(") && invocation.getParent() instanceof CtBlock;
    }

    static boolean isArray(CtTypeReference type) {
        return type.toString().contains("[]");
    }

    static boolean isPrimitive(CtTypeReference type) {
        try {
            return type.unbox().isPrimitive();
        } catch (SpoonClassNotFoundException e) {
            return false;
        }
    }
}
