package eu.stamp_project.test_framework.implementations;

import eu.stamp_project.test_framework.AbstractTestFrameworkDecorator;
import eu.stamp_project.test_framework.assertions.AssertEnum;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;

import java.util.Collections;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 */
public class GoogleTruthTestFramework extends AbstractTestFrameworkDecorator {

    public GoogleTruthTestFramework() {
        super("com.google.common.truth.Truth");
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments) {
        final Factory factory = testMethod.getFactory();
        //assertThat(actual)
        final CtInvocation<?> assertThat = createAssertThat(arguments.get(arguments.size() - 1));
        //isXXX(expected)
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setSimpleName(assertion.toStringAccordingToClass(this.getClass()));
        executableReference.setDeclaringType(factory.Type().createReference(assertion.toStringAccordingToClass(this.getClass())));
        invocation.setExecutable(executableReference);
        if (arguments.size() > 1) {
            invocation.setArguments(Collections.singletonList(arguments.get(0)));
        }
        invocation.setType(factory.Type().voidPrimitiveType());
        invocation.setTarget(assertThat);
        invocation.putMetadata(METADATA_ASSERT_AMPLIFICATION, true);
        return invocation;
    }

    @SuppressWarnings("unchecked")
    private CtInvocation<?> createAssertThat(CtExpression<?> actual) {
        final Factory factory = actual.getFactory();
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName(ASSERT_THAT);
        executableReference.setDeclaringType(factory.Type().createReference(this.qualifiedNameOfAssertClass));
        invocation.setExecutable(executableReference);
        invocation.setArguments(Collections.singletonList(actual));
        invocation.setType(factory.Type().voidPrimitiveType());
        invocation.setTarget(factory.createTypeAccess(factory.Type().createReference(this.qualifiedNameOfAssertClass)));
        invocation.putMetadata(METADATA_ASSERT_AMPLIFICATION, true);
        return invocation;
    }

    private static final String ASSERT_THAT = "assertThat";

    public static final String ASSERT_NULL = "isNull";
    public static final String ASSERT_NOT_NULL = "isNotNull";
    public static final String ASSERT_TRUE = "isTrue";
    public static final String ASSERT_FALSE = "isFalse";
    public static final String ASSERT_EQUALS = "isEqualTo";
    public static final String ASSERT_NOT_EQUALS = "isNotEqualTo";

    @Override
    public boolean isIgnored(CtElement candidate) {
        return false;
    }
}
