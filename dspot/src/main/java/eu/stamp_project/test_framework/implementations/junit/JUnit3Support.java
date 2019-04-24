package eu.stamp_project.test_framework.implementations.junit;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit3Support extends JUnitSupport {

    public JUnit3Support() {
        super("junit.framework.TestCase");
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationAfterClass() {
        return "";
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
        // check that the current test class inherit from TestCase
        final CtType<?> testClass = candidate.getParent(CtType.class);
        if (testClass == null) {
            return false;
        }
        return matchOneSuperClassToAssertClass(testClass.getReference()) &&
                //candidate.getAnnotations().isEmpty() && TODO checks if needed
                candidate.getSimpleName().startsWith("test");
    }

    private boolean matchOneSuperClassToAssertClass(CtTypeReference<?> currentTestClass) {
        if (currentTestClass.getSuperclass() == null) {
            return false;
        }
        return currentTestClass.getQualifiedName().equals(this.qualifiedNameOfAssertClass) ||
                matchOneSuperClassToAssertClass(currentTestClass.getSuperclass());
    }

    @Override
    public void generateAfterClassToSaveObservations(CtType<?> testClass, List<CtMethod<?>> testsToRun) {
        // TODO
    }
}