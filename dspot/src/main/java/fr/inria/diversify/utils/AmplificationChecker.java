package fr.inria.diversify.utils;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.SpoonException;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */

public class AmplificationChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplificationChecker.class);

    public static boolean isCase(CtLiteral literal) {
        return literal.getParent(CtCase.class) != null;
    }

    public static boolean isAssert(CtStatement stmt) {
        return stmt instanceof CtInvocation && isAssert((CtInvocation) stmt);
    }

    public static boolean isAssert(CtInvocation invocation) {
        // simplification of this method, rely on the name of the method, also,
        // we checks that this invocation is not an invocation to a method that contains assertion
        // in this case, we will match it
        final String nameOfMethodCalled = invocation.getExecutable().getSimpleName();
        return nameOfMethodCalled.toLowerCase().contains("assert") ||
                nameOfMethodCalled.toLowerCase().startsWith("fail") ||
                (invocation.getExecutable().getDeclaration() != null &&
                        !invocation.getExecutable()
                                .getDeclaration()
                                .getElements(new HasAssertInvocationFilter(3))
                                .isEmpty()
                );
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
        } catch (SpoonException e) {
            return false;
        }
    }

    public static boolean isInAssert(CtLiteral lit) {
        return lit.getParent(CtInvocation.class) != null &&
                AmplificationChecker.isAssert(lit.getParent(CtInvocation.class));
    }

    public static final TypeFilter<CtMethod<?>> IS_TEST_TYPE_FILTER = new TypeFilter<CtMethod<?>>(CtMethod.class) {
        @Override
        public boolean matches(CtMethod<?> element) {
            return AmplificationChecker.isTest(element);
        }
    };

    public static boolean isTest(CtMethod<?> candidate) {
        CtClass<?> parent = candidate.getParent(CtClass.class);
        // if the test method has @Ignore, is not a test
        if (candidate.getAnnotation(org.junit.Ignore.class) != null) {
            return false;
        }
        // if the test method is not visible, or has no body, or has parameters, is not a test
        if (candidate.isImplicit()
                || candidate.getVisibility() == null
                || !candidate.getVisibility().equals(ModifierKind.PUBLIC)
                || candidate.getBody() == null
                || candidate.getBody().getStatements().size() == 0
                || !candidate.getParameters().isEmpty()) {
            return false;
        }


        // Look for any assertion inside the test method
        List<CtInvocation> listOfAssertion =
                candidate.getBody().getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
                    @Override
                    public boolean matches(CtInvocation element) {
                        return hasAssertCall.test(element);
                    }
                });

        // If there are no assertions, look up inside the method called for assertions
        if (listOfAssertion.isEmpty()) {
            listOfAssertion.addAll(
                    candidate.getBody().getElements(new HasAssertInvocationFilter(3))
            );
        }

        if (!listOfAssertion.isEmpty()) { // there is at least one assertion
            return true;
        }

        return (candidate.getAnnotation(org.junit.Test.class) != null ||
                // matching JUnit3 test: the parent is not JUnit4 and the method's name starts with test or should
                ((candidate.getSimpleName().contains("test") || candidate.getSimpleName().contains("should"))
                        && !isTestJUnit4(parent))
        );
    }

    /**
     * checks if the given test class inherit from junit.framework.TestCase, <i>i.e.</i> is JUnit3 test class.
     * @param testClass
     * @return true if the given test class inherit from junit.framework.TestCase, false otherwise
     */
    public static boolean inheritFromTestCase(CtType<?> testClass) {
        return testClass.getSuperclass() != null &&
                "junit.framework.TestCase".equals(
                        testClass.getSuperclass().getQualifiedName());
    }

    private static class HasAssertInvocationFilter extends TypeFilter<CtInvocation> {
        int deep;

        HasAssertInvocationFilter(int deep) {
            super(CtInvocation.class);
            this.deep = deep;
        }

        @Override
        public boolean matches(CtInvocation element) {
            return deep >= 0 &&
                    (hasAssertCall.test(element) || containsMethodCallToAssertion(element, this.deep));
        }
    }

    private static final Predicate<CtInvocation<?>> hasAssertCall = invocation ->
            invocation.getExecutable() != null && invocation.getExecutable().getDeclaringType() != null &&
                    (invocation.getExecutable().getDeclaringType().equals(
                            invocation.getFactory().Type().createReference(Assert.class)
                    ) ||
                            invocation.getExecutable().getDeclaringType().equals(
                                    invocation.getFactory().Type().createReference(junit.framework.Assert.class)
                            ) || invocation.getExecutable().getDeclaringType().equals(
                            invocation.getFactory().Type().createReference(TestCase.class)
                    ));

    private static boolean containsMethodCallToAssertion(CtInvocation<?> invocation, int deep) {
        final CtMethod<?> method = invocation.getExecutable().getDeclaringType().getTypeDeclaration().getMethod(
                invocation.getExecutable().getType(),
                invocation.getExecutable().getSimpleName(),
                invocation.getExecutable().getParameters().toArray(new CtTypeReference[0])
        );
        return method != null && !method.getElements(new HasAssertInvocationFilter(deep - 1)).isEmpty();
    }

    public static boolean isTestJUnit4(CtType<?> classTest) {
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
            LOGGER.warn("Error during check of position of " + candidate.getSimpleName());
            return false;
        }
        return isTest(candidate);
    }
}
