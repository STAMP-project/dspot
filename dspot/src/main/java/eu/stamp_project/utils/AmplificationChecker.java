package eu.stamp_project.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.SpoonException;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.List;

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
        return _isAssert(invocation) ||
                (invocation.getExecutable().getDeclaration() != null &&
                        !invocation.getExecutable()
                                .getDeclaration()
                                .getElements(new HasAssertInvocationFilter(3))
                                .isEmpty()
                );
    }

    private final static List<String> ASSERTIONS_PACKAGES =
            Arrays.asList("org.junit", "com.google.common.truth", "org.assertj", "junit");

    private static boolean _isAssert(CtInvocation invocation) {
        // simplification of this method.
        // We rely on the package of the declaring type of the invocation
        // in this case, we will match it
        final String qualifiedNameOfPackage;
        if (invocation.getExecutable().getDeclaringType().getPackage() == null) {
            if (invocation.getExecutable().getDeclaringType().getTopLevelType() != null) {
                qualifiedNameOfPackage = invocation.getExecutable().getDeclaringType().getTopLevelType().getPackage().getQualifiedName();
            } else {
                return false;
            }
        } else {
            qualifiedNameOfPackage = invocation.getExecutable().getDeclaringType().getPackage().getQualifiedName();
        }
        return ASSERTIONS_PACKAGES.stream()
                .anyMatch(qualifiedNameOfPackage::startsWith);
    }

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

    private static boolean hasAnnotation(Class<?> classOfAnnotation, CtElement candidate) {
        return candidate.getAnnotations()
                .stream()
                .anyMatch(ctAnnotation -> ctAnnotation.getAnnotationType().getQualifiedName()
                        .equals(classOfAnnotation.getName()));

    }

    public static boolean isTest(CtMethod<?> candidate) {
        if (candidate == null) {
            return false;
        }
        CtClass<?> parent = candidate.getParent(CtClass.class);

        // if the test method has @Ignore, is not a test
        if (hasAnnotation(org.junit.Ignore.class, candidate)) {
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
                        return AmplificationChecker.isAssert(element);
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


        return (hasAnnotation(org.junit.Test.class, candidate) ||
                // matching JUnit3 test: the parent is not JUnit4 and the method's name starts with test or should
                ((candidate.getSimpleName().contains("test") || candidate.getSimpleName().contains("should"))
                        && !isTestJUnit4(parent))
        );
    }

    /**
     * checks if the given test class inherit from {@link junit.framework.TestCase}, <i>i.e.</i> is JUnit3 test class.
     *
     * @param testClass
     * @return true if the given test class inherit from {@link junit.framework.TestCase}, false otherwise
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
                    (AmplificationChecker._isAssert(element) || containsMethodCallToAssertion(element, this.deep));
        }
    }

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

    @Deprecated
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
