package eu.stamp_project.test_framework;

import eu.stamp_project.test_framework.assertions.IsAssertInvocationFilter;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 */
public abstract class AbstractTestFramework implements TestFrameworkSupport {

    private IsAssertInvocationFilter filter;

    protected final String qualifiedNameOfAssertClass;

    public AbstractTestFramework(String qualifiedNameOfAssertClass) {
        this.filter = new IsAssertInvocationFilter(qualifiedNameOfAssertClass);
        this.qualifiedNameOfAssertClass = qualifiedNameOfAssertClass;
    }

    @Override
    public boolean isAssert(CtInvocation<?> invocation) {
        return this.filter.isAssert(invocation);
    }

    @Override
    public boolean isAssert(CtStatement candidate) {
        if (candidate instanceof CtInvocation<?>) {
            return this.filter.isAssert(candidate);
        } else {
            return false;
        }
    }

    @Override
    public boolean isInAssert(CtElement candidate) {
        if (candidate.getParent(CtInvocation.class) != null) {
            return this.isAssert(candidate.getParent(CtInvocation.class));
        } else {
            return false;
        }
    }

}
