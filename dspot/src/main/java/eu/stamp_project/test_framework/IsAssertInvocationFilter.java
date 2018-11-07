package eu.stamp_project.test_framework;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class IsAssertInvocationFilter {

    private final String assertionClassName;

    /**
     * This field represent the number of method call to explore to find any assertion.
     * i.e. it will match a chain of method calls as follow:
     *  m1() -> m2() -> .... -> mdepth(), where only mdepth() contains an invocation to an assertion.
     */
    private final int depth;

    /**
     * @param assertionClassName the name of the class of the assertions used, e.g. org.junit.Assert.
     */
    public IsAssertInvocationFilter(String assertionClassName) {
        this.assertionClassName = assertionClassName;
        this.depth = 3;
    }

    /**
     * @param assertionClassName the name of the class of the assertions used, e.g. org.junit.Assert.
     * @param depth the number of method chained to find an assertion
     */
    public IsAssertInvocationFilter(String assertionClassName, int depth) {
        this.assertionClassName = assertionClassName;
        this.depth = depth;
    }

    /**
     * This method is a proxy for {@link IsAssertInvocationFilter#isAssert(CtInvocation)} for CtStatement.
     * @param candidate check if this CtStatement is directly an assertion call or an invocation to a method that contains assertion calls.
     * @return true if the candidate is an assertion or call a method that contains assertions.
     */
    public boolean isAssert(CtStatement candidate) {
        return candidate instanceof CtInvocation && isAssert((CtInvocation) candidate);
    }

    /**
     * @param invocation check if this CtInvocation is directly an assertion call or an invocation to a method that contains assertion calls.
     * @return true if the candidate is an assertion or call a method that contains assertions.
     */
    public boolean isAssert(CtInvocation<?> invocation) {
        return _isAssert(invocation) ||
                (invocation.getExecutable().getDeclaration() != null &&
                        !invocation.getExecutable()
                                .getDeclaration()
                                .getElements(new HasAssertInvocationFilter(this.depth))
                                .isEmpty()
                );
    }

    private class HasAssertInvocationFilter extends TypeFilter<CtInvocation<?>> {

        int deep;

        HasAssertInvocationFilter(int deep) {
            super(CtInvocation.class);
            this.deep = deep;
        }

        @Override
        public boolean matches(CtInvocation<?> element) {
            return deep >= 0 &&
                    (_isAssert(element) || containsMethodCallToAssertion(element, this.deep));
        }
    }

    private boolean _isAssert(CtInvocation<?> invocation) {
        // We rely on the package of the declaring type of the invocation
        // in this case, we will match it
        final String qualifiedNameOfDeclaringType;
        // TODO should make these checks?
        if (invocation == null ||
                invocation.getExecutable() == null ||
                invocation.getExecutable().getDeclaringType() == null ||
                invocation.getExecutable().getDeclaringType().getTypeDeclaration() == null) {
            return false;
        }
        if (invocation.getExecutable().getDeclaringType().getPackage() == null) {
            if (invocation.getExecutable().getDeclaringType().getTopLevelType() != null) {
                qualifiedNameOfDeclaringType = invocation.getExecutable().getDeclaringType().getTopLevelType().getQualifiedName();
            } else {
                return false;
            }
        } else {
            qualifiedNameOfDeclaringType = invocation.getExecutable().getDeclaringType().getQualifiedName();
        }
        return Pattern.compile(this.assertionClassName).matcher(qualifiedNameOfDeclaringType).matches();
    }

    private boolean containsMethodCallToAssertion(CtInvocation<?> invocation, int deep) {
        if (invocation.getExecutable() == null ||
                invocation.getExecutable().getDeclaringType() == null ||
                invocation.getExecutable().getDeclaringType().getTypeDeclaration() == null) {
            return false;
        }
        final CtMethod<?> method = invocation.getExecutable().getDeclaringType().getTypeDeclaration().getMethod(
                invocation.getExecutable().getType(),
                invocation.getExecutable().getSimpleName(),
                invocation.getExecutable().getParameters().toArray(new CtTypeReference[0])
        );
        return method != null && !method.getElements(new HasAssertInvocationFilter(deep - 1)).isEmpty();
    }

    @Deprecated
    private final static List<String> ASSERTIONS_PACKAGES =
            Arrays.asList(
                    "org.junit.Assert", // JUnit 4
                    "org.junit.jupiter.api.Assertions", // JUnit 5
                    "com.google.common.truth.*", // TODO Truth
                    "org.assertj.core.api.*", //  assertJ
                    "junit.framework.TestCase" // JUnit 3
            );
}
