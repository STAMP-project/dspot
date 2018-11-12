package eu.stamp_project.test_framework;

import eu.stamp_project.test_framework.junit.JUnitSupport;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
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
public class GoogleTruthTestFramework extends AbstractTestFramework {

    public GoogleTruthTestFramework() {
        super("com.google.common.truth.Truth");
    }

    @Override
    public boolean isTest(CtMethod<?> candidate) throws UnsupportedTestFrameworkException {
        return false;
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments) {
        final Factory factory = InputConfiguration.get().getFactory();
        //assertThat(actual)
        final CtInvocation<?> assertThat = createAssertThat(arguments.get(arguments.size() - 1));
        //isXXX(expected)
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setSimpleName(assertEnumToMethodName(assertion));
        executableReference.setDeclaringType(factory.Type().createReference(assertEnumToMethodName(assertion)));
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
        final Factory factory = InputConfiguration.get().getFactory();
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

    private String assertEnumToMethodName(AssertEnum assertEnum) {
        try {
            return (String ) GoogleTruthTestFramework.class.getDeclaredField(assertEnum.name()).get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String ASSERT_THAT = "assertThat";

    private static final String ASSERT_NULL = "isNull";
    private static final String ASSERT_NOT_NULL = "isNotNull";
    private static final String ASSERT_TRUE = "isTrue";
    private static final String ASSERT_FALSE = "isFalse";
    private static final String ASSERT_EQUALS = "isEqualsTo";
    private static final String ASSERT_NOT_EQUALS = "isNotEqualsTo";

}
