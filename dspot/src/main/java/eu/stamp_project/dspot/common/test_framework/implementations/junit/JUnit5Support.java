package eu.stamp_project.dspot.common.test_framework.implementations.junit;

import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.testrunner.runner.Failure;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Arrays;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit5Support extends JUnitSupport {

    public JUnit5Support() {
        super("org.junit.jupiter.api.Assertions");
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationAfterClass() {
        return "org.junit.jupiter.api.AfterAll";
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
    public CtMethod<?> generateExpectedExceptionsBlock(CtMethod<?> test, Failure failure, int numberOfFail) {
        final Factory factory = test.getFactory();

        final CtLambda lambda = factory.createLambda();
        lambda.setBody(test.getBody());

        // invocation to assertThrows
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName("assertThrows");
        executableReference.setDeclaringType(factory.Type().createReference(this.qualifiedNameOfAssertClass));
        invocation.setExecutable(executableReference);
        invocation.setType(factory.Type().voidPrimitiveType());
        invocation.setTarget(factory.createTypeAccess(factory.Type().createReference(this.qualifiedNameOfAssertClass)));

        final CtTypeReference<?> ctTypeOfException = factory.Type().createReference(failure.fullQualifiedNameOfException);
        final CtFieldRead<?> fieldRead = factory.createFieldRead();
        fieldRead.setTarget(factory.createTypeAccess(ctTypeOfException));
        final CtFieldReference fieldReference = factory.createFieldReference();
        fieldReference.setDeclaringType(ctTypeOfException);
        fieldReference.setSimpleName("class");
        fieldRead.setVariable(fieldReference);

        invocation.setArguments(
                Arrays.asList(
                        fieldRead,
                        lambda
                )
        );
        CtBlock body = factory.Core().createBlock();
        body.addStatement(invocation);
        DSpotUtils.addComment(invocation,
                "AssertionGenerator generate try/catch block with fail statement",
                CtComment.CommentType.INLINE,
                CommentEnum.Amplifier);

        test.setBody(body);
        test.setSimpleName(test.getSimpleName() + "_failAssert" + (numberOfFail));

        return test;
    }
}
