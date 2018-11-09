package eu.stamp_project.test_framework.junit;

import eu.stamp_project.test_framework.IsAssertInvocationFilter;
import eu.stamp_project.test_framework.TestFrameworkSupport;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 *
 * This abstract class is used for JUnit4 and JUnit5 support
 */
public abstract class JUnitSupport implements TestFrameworkSupport {

    protected abstract String getFullQualifiedNameOfClassWithAssertions();

    protected abstract String getFullQualifiedNameOfAnnotationTest();

    protected abstract String getFullQualifiedNameOfAnnotationIgnore();

    protected boolean isIgnored(CtMethod<?> candidate) {
        return this.hasAnnotation(getFullQualifiedNameOfAnnotationIgnore(), candidate);
    }

    protected boolean isATest(CtMethod<?> candidate) {
        return hasAnnotation(getFullQualifiedNameOfAnnotationTest(), candidate);
    }

    private IsAssertInvocationFilter filter;

    public JUnitSupport() {
        this.filter = new IsAssertInvocationFilter(this.getFullQualifiedNameOfClassWithAssertions());
    }

    @Override
    public boolean isAssert(CtInvocation<?> invocation) {
        return this.filter.isAssert(invocation);
    }

    @Override
    public boolean isAssert(CtStatement candidate) {
        return this.filter.isAssert(candidate);
    }

    @Override
    public boolean isInAssert(CtElement candidate) {
        if (candidate.getParent(CtInvocation.class) != null) {
            return this.isAssert(candidate.getParent(CtInvocation.class));
        } else {
            return false;
        }
    }

    @Override
    public boolean isTest(CtMethod<?> candidate) {
        if (candidate == null) {
            return false;
        }
        // if the test method has @Ignore, is not a test
        if (isIgnored(candidate)) {
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
        // is a test according to the JUnit version
        return isATest(candidate);
    }

    private boolean hasAnnotation(String fullQualifiedNameOfAnnotation, CtElement candidate) {
        return candidate.getAnnotations()
                .stream()
                .anyMatch(ctAnnotation -> ctAnnotation.getAnnotationType().getQualifiedName()
                        .equals(fullQualifiedNameOfAnnotation));

    }


}
