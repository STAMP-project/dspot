package eu.stamp_project.test_framework.junit;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit3Support extends JUnitSupport {

    @Override
    protected String getFullQualifiedNameOfClassWithAssertions() {
        return "junit.framework.TestCase";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationTest() {
        return "";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationIgnore() {
        return "";
    }

    /*
        For JUnit3, a test method starts by test, otherwise we consider ignored
     */
    @Override
    protected boolean isIgnored(CtMethod<?> candidate) {
        return !candidate.getSimpleName().startsWith("test");
    }

    /*
        For JUnit3, a test method starts by test.
     */
    @Override
    protected boolean isATest(CtMethod<?> candidate) {
        return candidate.getAnnotations().isEmpty() && candidate.getSimpleName().startsWith("test");
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion() {
        return null;
    }
}
