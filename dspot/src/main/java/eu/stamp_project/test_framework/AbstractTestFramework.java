package eu.stamp_project.test_framework;

import eu.stamp_project.test_framework.assertions.IsAssertInvocationFilter;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 */
public abstract class AbstractTestFramework implements TestFrameworkSupport {

    private IsAssertInvocationFilter filter;

    protected final List<String> qualifiedNameOfAssertClasses;

    protected final String qualifiedNameOfAssertClass;

    public AbstractTestFramework(String... qualifiedNameOfAssertClasses) {
        this.qualifiedNameOfAssertClasses = Arrays.asList(qualifiedNameOfAssertClasses);
        this.qualifiedNameOfAssertClass = this.qualifiedNameOfAssertClasses.get(0);
        this.filter = new IsAssertInvocationFilter(this.qualifiedNameOfAssertClasses);
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

    // here, we get the correct name of the expected Exception.
    // in JUnit, we use the name 'expected' for the exception in a try/catch block
    // however, DSpot can generate several such block
    // this return 'expected' + the number of catch block in the test
    public String getCorrectExpectedNameOfException(CtMethod<?> test) {
        String expectedName = "expected";
        final List<CtCatch> catches =
                test.getElements(new TypeFilter<>(CtCatch.class));
        if (catches.isEmpty()) {
            return expectedName;
        } else {
            return expectedName + "_" + catches.size();
        }
    }

}
