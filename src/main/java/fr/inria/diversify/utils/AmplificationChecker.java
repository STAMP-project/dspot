package fr.inria.diversify.utils;

import fr.inria.diversify.util.Log;
import junit.framework.TestCase;
import org.junit.Assert;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.List;
import java.util.function.Predicate;

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
            return (mthName.startsWith("assert") ||
                    mthName.contains("fail")) ||
                    isAssertInstance(cl);
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
        return !invocation.toString().startsWith("super(");// && invocation.getParent() instanceof CtBlock;
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
        return lit.getParent(CtInvocation.class) != null &&
                AmplificationChecker.isAssert(lit.getParent(CtInvocation.class));
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

        List<CtInvocation> listOfAssertion = candidate.getBody().getElements(filterContainsAssertions);

        return candidate.getParameters().isEmpty() &&
                (candidate.getAnnotation(org.junit.Test.class) != null ||
                        ((candidate.getSimpleName().contains("test") ||
                                candidate.getSimpleName().contains("should")) && !isTestJUnit4(parent)
                                && !listOfAssertion.isEmpty()));
    }

    private static final TypeFilter<CtInvocation> filterContainsAssertions = new TypeFilter<CtInvocation>(CtInvocation.class) {
        @Override
        public boolean matches(CtInvocation element) {
            return element.getExecutable() != null && element.getExecutable().getDeclaringType() != null &&
                    (element.getExecutable().getDeclaringType().equals(
                            element.getFactory().Type().createReference(Assert.class)
                    ) ||
                            element.getExecutable().getDeclaringType().equals(
                                    element.getFactory().Type().createReference(junit.framework.Assert.class)
                            ) || element.getExecutable().getDeclaringType().equals(
                            element.getFactory().Type().createReference(TestCase.class)
                    )) ||
                    (containsMethodCallToAssertion.test(element));
        }
    };

    private static final Predicate<CtInvocation<?>> containsMethodCallToAssertion = invocation -> {
        final CtMethod<?> method = invocation.getExecutable().getDeclaringType().getTypeDeclaration().getMethod(
                invocation.getExecutable().getType(),
                invocation.getExecutable().getSimpleName(),
                invocation.getExecutable().getParameters().stream().toArray(CtTypeReference[]::new)
        );
        return method != null && !method.getElements(filterContainsAssertions).isEmpty();
    };

    private static boolean isTestJUnit4(CtClass<?> classTest) {
        return classTest.getMethods().stream()
                .anyMatch(ctMethod ->
                        ctMethod.getAnnotation(org.junit.Test.class) != null
                );
    }

    public static boolean isTest(CtMethod candidate, String relativePath) {
        try {
            if (!relativePath.isEmpty() && candidate.getPosition() != null
                    && candidate.getPosition().getFile() != null
                    && !candidate.getPosition().getFile().toString().replaceAll("\\\\", "/").contains(relativePath)) {
                return false;
            }
        } catch (Exception e) {
            Log.warn("Error during check of position of " + candidate.getSimpleName());
            return false;
        }
        return isTest(candidate);
    }
}
