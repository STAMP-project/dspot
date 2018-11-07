package eu.stamp_project.test_framework.junit;

import spoon.reflect.code.CtInvocation;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit5Support extends JUnitSupport {

    @Override
    protected String getFullQualifiedNameOfClassWithAssertions() {
        return "org.junit.jupiter.api.Assertions";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationTest() {
        return "org.junit.jupiter.api.Test";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationIgnore() {
        return "org.junit.jupiter.api.Disabled";
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion() {
        return null;
    }
}
