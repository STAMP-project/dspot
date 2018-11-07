package eu.stamp_project.test_framework.junit;

import spoon.reflect.code.CtInvocation;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit4Support extends JUnitSupport {

    @Override
    protected String getFullQualifiedNameOfClassWithAssertions() {
        return "org.junit.Assert";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationTest() {
        return "org.junit.Test";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationIgnore() {
        return "org.junit.Ignore";
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion() {
        return null;
    }
}
