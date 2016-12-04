package fr.inria.diversify.dspot.amp;

import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;

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
}
