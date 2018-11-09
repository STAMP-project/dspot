package eu.stamp_project.test_framework.junit;

import eu.stamp_project.test_framework.AbstractTestFramework;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 * <p>
 * This abstract class is used for JUnit4 and JUnit5 support
 */
public abstract class JUnitSupport extends AbstractTestFramework {

    protected final String qualifiedNameOfAssertClass;

    protected String getFullQualifiedNameOfAssertClass() {
        return this.qualifiedNameOfAssertClass;
    }

    protected abstract String getFullQualifiedNameOfAnnotationTest();

    protected abstract String getFullQualifiedNameOfAnnotationIgnore();

    protected boolean isIgnored(CtMethod<?> candidate) {
        return this.hasAnnotation(getFullQualifiedNameOfAnnotationIgnore(), candidate);
    }

    protected boolean isATest(CtMethod<?> candidate) {
        return hasAnnotation(getFullQualifiedNameOfAnnotationTest(), candidate);
    }

    public JUnitSupport(String qualifiedNameOfAssertClass) {
        super(qualifiedNameOfAssertClass);
        this.qualifiedNameOfAssertClass = qualifiedNameOfAssertClass;
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

    /**
     *  key of metadata of spoon nodes to know if its result from amplification
     */
    public final static String METADATA_ASSERT_AMPLIFICATION = "A-Amplification";

    /**
     * Builds an invocation to <code>methodName</code> of {@link org.junit.Assert}.
     * This should be a correct method name such as assertEquals, assertTrue...
     *
     * @param methodName the name of the assertion method
     * @param arguments  the arguments of the assertion, <i>e.g.</i> the two element to be compared in {@link org.junit.Assert#assertEquals(Object, Object)}
     * @return a spoon node representing the invocation to the assertion, ready to be inserted in a test method
     */
    @Override
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, String methodName, List<CtExpression> arguments) {
        final Factory factory = InputConfiguration.get().getFactory();
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName(methodName);
        executableReference.setDeclaringType(factory.Type().createReference(this.qualifiedNameOfAssertClass));
        invocation.setExecutable(executableReference);
        invocation.setArguments(arguments); // TODO
        invocation.setType(factory.Type().voidPrimitiveType());
        invocation.setTarget(factory.createTypeAccess(factory.Type().createReference(this.qualifiedNameOfAssertClass)));
        invocation.putMetadata(METADATA_ASSERT_AMPLIFICATION, true);
        return invocation;
    }
}
