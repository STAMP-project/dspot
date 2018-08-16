package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/05/18
 */
public class TryCatchFailGenerator {

    private int numberOfFail;

    public TryCatchFailGenerator() {
        this.numberOfFail = 0;
    }

    /**
     * Adds surrounding try/catch/fail in a failing test.
     *
     * @param test Failing test method to amplify
     * @param failure Test's failure description
     * @return New amplified test
     */
    @SuppressWarnings("unchecked")
    public CtMethod<?> surroundWithTryCatchFail(CtMethod<?> test, Failure failure) {
        CtMethod cloneMethodTest = CloneHelper.cloneTestMethodForAmp(test, "");
        cloneMethodTest.setSimpleName(test.getSimpleName());
        Factory factory = cloneMethodTest.getFactory();

        // TestTimedOutException means infinite loop
        // AssertionError means that some assertion remained in the test: TODO
        if ("org.junit.runners.model.TestTimedOutException".equals(failure.fullQualifiedNameOfException) ||
                "java.lang.OutOfMemoryError".equals(failure.fullQualifiedNameOfException) ||
                "java.lang.StackOverflowError".equals(failure.fullQualifiedNameOfException) ||
                "java.lang.AssertionError".equals(failure.fullQualifiedNameOfException)) {
            return null;
        }

        final String[] split = failure.fullQualifiedNameOfException.split("\\.");
        final String simpleNameOfException = split[split.length - 1];

        CtTry tryBlock = factory.Core().createTry();
        tryBlock.setBody(cloneMethodTest.getBody());
        String snippet = "org.junit.Assert.fail(\"" + test.getSimpleName() + " should have thrown " + simpleNameOfException + "\")";
        tryBlock.getBody().addStatement(factory.Code().createCodeSnippetStatement(snippet));
        DSpotUtils.addComment(tryBlock, "AssertGenerator generate try/catch block with fail statement", CtComment.CommentType.INLINE);

        CtCatch ctCatch = factory.Core().createCatch();
        CtTypeReference exceptionType = factory.Type().createReference(failure.fullQualifiedNameOfException);
        ctCatch.setParameter(factory.Code().createCatchVariable(exceptionType, getCorrectExpectedNameOfException(test)));

        ctCatch.setBody(factory.Core().createBlock());

        List<CtCatch> catchers = new ArrayList<>(1);
        catchers.add(ctCatch);
        addAssertionOnException(ctCatch, failure);
        tryBlock.setCatchers(catchers);

        CtBlock body = factory.Core().createBlock();
        body.addStatement(tryBlock);

        cloneMethodTest.setBody(body);
        cloneMethodTest.setSimpleName(cloneMethodTest.getSimpleName() + "_failAssert" + (numberOfFail++));
        Counter.updateAssertionOf(cloneMethodTest, 1);

        return cloneMethodTest;
    }

    private void addAssertionOnException(CtCatch ctCatch, Failure failure) {
        final Factory factory = ctCatch.getFactory();
        final CtCatchVariable<? extends Throwable> parameter = ctCatch.getParameter();
        final CtInvocation<?> getMessage = factory.createInvocation(
                factory.createVariableRead(parameter.getReference(), false),
                factory.Class().get(java.lang.Throwable.class).getMethodsByName("getMessage").get(0).getReference()
        );
        if (!AssertGeneratorHelper.containsObjectReferences(failure.messageOfFailure)) {
            ctCatch.getBody().addStatement(
                    AssertGeneratorHelper.buildInvocation(factory,
                            "assertEquals",
                            Arrays.asList(factory.createLiteral(failure.messageOfFailure), getMessage)
                    )
            );
        }
    }

    // here, we get the correct name of the expected Exception.
    // in JUnit, we use the name 'expected' for the exception in a try/catch block
    // however, DSpot can generate several such block
    // this return 'expected' + the number of catch block in the test
    private String getCorrectExpectedNameOfException(CtMethod<?> test) {
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
