package fr.inria.diversify.testRunner;

import fr.inria.diversify.util.Log;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/02/16
 * Time: 10:34
 */
public class JunitResult extends RunListener {
    Set<Description> testRuns;
    List<Failure> failures;

    public JunitResult() {
        testRuns = new HashSet<>();
        failures = new ArrayList<>();
    }

    public void testFinished(Description description) throws Exception {
        testRuns.add(description);
    }

    public void testFailure(Failure failure) throws Exception {
        if(!isCompileOrTimeOutError(failure)) {
            testRuns.add(failure.getDescription());
            failures.add(failure);
        }
    }

    public void testAssumptionFailure(Failure failure) {
        if(!isCompileOrTimeOutError(failure)) {
            testRuns.add(failure.getDescription());
            failures.add(failure);
        }
    }

    protected boolean isCompileOrTimeOutError(Failure failure) {
        String exceptionMessage = failure.getException().getMessage();
        if(exceptionMessage == null) {
            return false;
        } else {
            return exceptionMessage.contains("Unresolved compilation problem")
                    || exceptionMessage.contains("test timed out after");
        }
    }

    public List<String> runTests() {
        return testRuns.stream()
                .map(description -> description.getMethodName())
                .collect(Collectors.toList());
    }

    public List<String> goodTests() {
        List<String> failureTestNames = failureTests();
        return testRuns.stream()
                .map(description -> description.getMethodName())
                .filter(testName -> !failureTestNames.contains(testName))
                .collect(Collectors.toList());
    }

    public List<String> failureTests() {
        return failures.stream()
                .map(failure -> failure.getDescription())
                .map(description -> description.getMethodName())
                .collect(Collectors.toList());
    }

    public List<Failure> getFailures() {
        return failures;
    }
}
