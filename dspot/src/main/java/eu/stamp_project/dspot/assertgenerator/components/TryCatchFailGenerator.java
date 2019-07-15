package eu.stamp_project.dspot.assertgenerator.components;

import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.Counter;
import spoon.reflect.declaration.CtMethod;

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

    private final static List<String> UNSUPPORTED_EXCEPTION = Arrays.asList(
            "junit.framework.ComparisonFailure",
            "org.junit.runners.model.TestTimedOutException",
            "java.lang.OutOfMemoryError",
            "java.lang.StackOverflowError",
            "java.lang.AssertionError",
            "org.opentest4j.AssertionFailedError"
    );

    /**
     * Adds surrounding try/catch/fail in a failing test.
     *
     * @param test    Failing test method to amplify
     * @param failure Test's failure description
     * @return New amplified test
     */
    @SuppressWarnings("unchecked")
    public CtMethod<?> surroundWithTryCatchFail(CtMethod<?> test, Failure failure) {
        CtMethod cloneMethodTest = CloneHelper.cloneTestMethodForAmp(test, "");
        cloneMethodTest.setSimpleName(test.getSimpleName());
        // TestTimedOutException means infinite loop
        // AssertionError means that some assertion remained in the test: TODO
        if (UNSUPPORTED_EXCEPTION.contains(failure.fullQualifiedNameOfException)) {
            return null;
        }
        cloneMethodTest = TestFramework.get().generateExpectedExceptionsBlock(cloneMethodTest, failure, this.numberOfFail);
        Counter.updateAssertionOf(cloneMethodTest, 1);
        return cloneMethodTest;
    }

}
