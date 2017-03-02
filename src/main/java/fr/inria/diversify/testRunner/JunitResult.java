package fr.inria.diversify.testRunner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/02/16
 * Time: 10:34
 */
public class JunitResult extends RunListener {

    private Set<Description> testRuns;
    private List<Failure> failures;
    private List<Failure> compileOrTimeOutError;

    public JunitResult() {
        testRuns = new HashSet<>();
        failures = new ArrayList<>();
        compileOrTimeOutError = new ArrayList<>();
    }

    public synchronized JunitResult add(JunitResult other) {
        this.testRuns.addAll(other.testRuns);
        this.failures.addAll(other.failures);
        this.compileOrTimeOutError.addAll(other.compileOrTimeOutError);
        return this;
    }

    public synchronized void testFinished(Description description) throws Exception {
        testRuns.add(description);
    }

    public synchronized void testFailure(Failure failure) throws Exception {
        if(!isCompileOrTimeOutError(failure)) {
            testRuns.add(failure.getDescription());
            failures.add(failure);
        } else {
            compileOrTimeOutError.add(failure);
        }
    }

    public synchronized void testAssumptionFailure(Failure failure) {
        //empty
    }

    private synchronized boolean isCompileOrTimeOutError(Failure failure) {
        String exceptionMessage = failure.getException().getMessage();
        if(exceptionMessage == null) {
            return false;
        } else {
            return exceptionMessage.contains("Unresolved compilation problem")
                    || exceptionMessage.contains("test timed out after");
        }
    }

    public synchronized List<String> runTests() {
        List<Description> descriptionsTestRuns = new ArrayList<>(testRuns);
        return descriptionsTestRuns.stream()
                .map(description -> description.getMethodName())
                .collect(Collectors.toList());
    }

    public synchronized List<String> goodTests() {
        List<String> failureTestNames = failureTests();
        List<String> compileOrTimeOutTestName = compileOrTimeOutTestName();
        List<Description> descriptionsTestRuns = new ArrayList<>(testRuns);
        return descriptionsTestRuns.stream()
                .map(description -> description.getMethodName())
                .filter(testName -> !failureTestNames.contains(testName))
                .filter(testName -> !compileOrTimeOutTestName.contains(testName))
                .collect(Collectors.toList());
    }

    public synchronized List<String> compileOrTimeOutTestName() {
        return compileOrTimeOutError.stream()
                .map(failure -> failure.getDescription())
                .map(description -> description.getMethodName())
                .collect(Collectors.toList());
    }

    public synchronized List<String> failureTests() {
        return failures.stream()
                .map(failure -> failure.getDescription())
                .map(description -> description.getMethodName())
                .collect(Collectors.toList());
    }

    public synchronized List<Failure> getFailures() {
        return failures;
    }

    public synchronized List<String> getTestRuns() {
        return testRuns.stream()
                .map(description -> description.getMethodName())
                .collect(Collectors.toList());
    }
}
