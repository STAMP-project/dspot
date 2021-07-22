package eu.stamp_project.dspot.common.test_framework.implementations.junit;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.ObjectLog;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.test_framework.AbstractTestFramework;
import eu.stamp_project.dspot.common.test_framework.assertions.AssertEnum;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 * <p>
 * This abstract class is used for JUnit4 and JUnit5 support
 */
public abstract class JUnitSupport extends AbstractTestFramework {

    protected abstract String getFullQualifiedNameOfAnnotationTest();

    protected abstract String getFullQualifiedNameOfAnnotationIgnore();

    protected abstract String getFullQualifiedNameOfAnnotationAfterClass();

    public boolean isIgnored(CtElement candidate) {
        return this.hasAnnotation(getFullQualifiedNameOfAnnotationIgnore(), candidate);
    }

    protected boolean isATest(CtMethod<?> candidate) {
        return hasAnnotation(getFullQualifiedNameOfAnnotationTest(), candidate);
    }

    public JUnitSupport(String... qualifiedNameOfAssertClass) {
        super(qualifiedNameOfAssertClass);
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
        // if the test method has no body, or has parameters, is not a test
        if (candidate.isImplicit()
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
     * Builds an invocation to <code>methodName</code> of {@link org.junit.Assert}.
     * This should be a correct method name such as assertEquals, assertTrue...
     *
     * @param assertion the type of the assertion method
     * @param arguments  the arguments of the assertion, <i>e.g.</i> the two element to be compared in {@link org.junit.Assert#assertEquals(Object, Object)}
     * @return a spoon node representing the invocation to the assertion, ready to be inserted in a test method
     */
    @Override
    public CtInvocation<?> buildInvocationToAssertion(CtMethod<?> testMethod, AssertEnum assertion, List<CtExpression> arguments) {
        final Factory factory = testMethod.getFactory();
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName(assertion.toStringAccordingToClass(JUnitSupport.class));
        executableReference.setDeclaringType(factory.Type().createReference(this.qualifiedNameOfAssertClass));
        invocation.setExecutable(executableReference);
        invocation.setArguments(arguments); // TODO
        invocation.setType(factory.Type().voidPrimitiveType());
        invocation.setTarget(factory.createTypeAccess(factory.Type().createReference(this.qualifiedNameOfAssertClass)));
        invocation.putMetadata(METADATA_ASSERT_AMPLIFICATION, true);
        return invocation;
    }

    @Override
    public CtMethod<?> prepareTestMethod(CtMethod<?> testMethod) {
        if (testMethod.getThrownTypes().isEmpty()) {
            testMethod.addThrownType(
                    testMethod.getFactory()
                            .Type()
                            .createReference(Exception.class)
            );
        }
        return testMethod;
    }

    public static final String ASSERT_NULL = "assertNull";
    public static final String ASSERT_NOT_NULL = "assertNotNull";
    public static final String ASSERT_TRUE = "assertTrue";
    public static final String ASSERT_FALSE = "assertFalse";
    public static final String ASSERT_EQUALS = "assertEquals";
    public static final String ASSERT_NOT_EQUALS = "assertNotEquals";
    public static final String ASSERT_ARRAY_EQUALS = "assertArrayEquals";

    @Override
    public CtMethod<?> generateExpectedExceptionsBlock(CtMethod<?> test, Failure failure, int numberOfFail) {
        final Factory factory = test.getFactory();

        final String[] split = failure.fullQualifiedNameOfException.split("\\.");
        final String simpleNameOfException = split[split.length - 1];

        CtTry tryBlock = factory.Core().createTry();
        tryBlock.setBody(test.getBody());

        CtType<?> assertClass = factory.createReference(this.qualifiedNameOfAssertClass).getTypeDeclaration();
        CtStatement failStatement;
        if (assertClass.getMethod("fail") == null) {
            String snippet = this.qualifiedNameOfAssertClass + ".fail(\"" + test.getSimpleName() + " should have thrown " + simpleNameOfException + "\")";
            failStatement = factory.Code().createCodeSnippetStatement(snippet);
        } else {
            final CtMethod<?> fail = assertClass.getMethod("fail");
            final CtTypeAccess<?> assertTypeAccess = factory.createTypeAccess(assertClass.getReference());
            failStatement = factory.createInvocation(
                    assertTypeAccess,
                    fail.getReference()
            ).addArgument(
                    factory.createLiteral(
                            test.getSimpleName() + " should have thrown " + simpleNameOfException
                    )
            );
        }
        tryBlock.getBody().addStatement(failStatement);
        DSpotUtils.addComment(tryBlock,
                "AssertionGenerator generate try/catch block with fail statement",
                CtComment.CommentType.INLINE,
                CommentEnum.Amplifier);

        CtCatch ctCatch = factory.Core().createCatch();
        CtTypeReference exceptionType = factory.Type().createReference(failure.fullQualifiedNameOfException);
        ctCatch.setParameter(factory.Code().createCatchVariable(exceptionType, this.getCorrectExpectedNameOfException(test)));

        ctCatch.setBody(factory.Core().createBlock());

        List<CtCatch> catchers = new ArrayList<>(1);
        catchers.add(ctCatch);
        addAssertionOnException(test, ctCatch, failure);
        tryBlock.setCatchers(catchers);

        CtBlock body = factory.Core().createBlock();
        body.addStatement(tryBlock);

        test.setBody(body);
        test.setSimpleName(test.getSimpleName() + "_failAssert" + (numberOfFail));

        return test;
    }

    private void addAssertionOnException(CtMethod<?> testMethod, CtCatch ctCatch, Failure failure) {
        final Factory factory = ctCatch.getFactory();
        final CtCatchVariable<? extends Throwable> parameter = ctCatch.getParameter();
        final CtInvocation<?> getMessage = factory.createInvocation(
                factory.createVariableRead(parameter.getReference(), false),
                factory.Class().get(java.lang.Throwable.class).getMethodsByName("getMessage").get(0).getReference()
        );
        if (AssertionGeneratorUtils.canGenerateAnAssertionFor(failure.messageOfFailure)) {
            ctCatch.getBody().addStatement(
                    this.buildInvocationToAssertion(
                            testMethod,
                            AssertEnum.ASSERT_EQUALS,
                            Arrays.asList(factory.createLiteral(failure.messageOfFailure), getMessage)
                    )
            );
        }
    }

    @Override
    public void generateAfterClassToSaveObservations(CtType<?> testClass, List<CtMethod<?>> testsToRun) {
        // get AfterClassMethod is exist otherwise use initAfterClassMethod
        final Factory factory = testClass.getFactory();
        final CtMethod<?> afterClassMethod = testClass.getMethods()
                .stream()
                .filter(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        this.getFullQualifiedNameOfAnnotationAfterClass().equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ).findFirst()
                .orElse(initAfterClassMethod(factory));
        createCallToSaveAndInsertAtTheEnd(factory, afterClassMethod);
        testClass.addMethod(afterClassMethod);
    }

    protected void createCallToSaveAndInsertAtTheEnd(Factory factory, CtMethod<?> afterClassMethod) {
        final CtTypeReference<?> ctTypeReference = factory.createCtTypeReference(ObjectLog.class);
        final CtExecutableReference<?> reference = ctTypeReference
                .getTypeDeclaration()
                .getMethodsByName("save")
                .get(0)
                .getReference();
        if (afterClassMethod.getBody() == null) {
            afterClassMethod.setBody(
                    factory.createInvocation(factory.createTypeAccess(ctTypeReference), reference)
            );
        } else {
            afterClassMethod.getBody().insertEnd(
                    factory.createInvocation(factory.createTypeAccess(ctTypeReference), reference)
            );
        }
    }

    private CtMethod<Void> initAfterClassMethod(Factory factory) {
        final CtMethod<Void> afterClassMethod = factory.createMethod();
        afterClassMethod.setType(factory.Type().VOID_PRIMITIVE);
        afterClassMethod.addModifier(ModifierKind.PUBLIC);
        afterClassMethod.addModifier(ModifierKind.STATIC);
        afterClassMethod.setSimpleName("afterClass");
        final CtAnnotation annotation = factory.createAnnotation();
        annotation.setAnnotationType(factory.Annotation().create(this.getFullQualifiedNameOfAnnotationAfterClass()).getReference());
        afterClassMethod.addAnnotation(annotation);
        afterClassMethod.setBody(factory.createBlock());
        return afterClassMethod;
    }
}
