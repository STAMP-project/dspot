package fr.inria.diversify.testRunner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/02/16
 * Time: 10:34
 */
public class JunitResult extends RunListener {

    private List<String> testsRun;
    private Map<String, Failure> testsFail;

    public JunitResult() {
        this.testsRun = new ArrayList<>();
        this.testsFail = new HashMap<>();

    }

    @Override
    public synchronized void testFinished(Description description) throws Exception {
        this.testsRun.add(description.getMethodName());
    }

    @Override
    public synchronized void testFailure(Failure failure) throws Exception {
        this.testsFail.put(failure.getDescription().getMethodName(), failure);
    }

    @Override
    public synchronized void testAssumptionFailure(Failure failure) {
        //empty
    }

    public List<String> getPassingTests() {
        return this.testsRun.stream()
                .filter(methodName -> !this.testsFail.keySet().contains(methodName))
                .collect(Collectors.toList());
    }

    public Failure getFailureOf(String testCaseName) {
        return this.testsFail.get(testCaseName);
    }

    public Set<String> getFailures() {
        return this.testsFail.keySet();
    }

    public List<String> getTestsRun() {
        return this.testsRun;
    }
}
