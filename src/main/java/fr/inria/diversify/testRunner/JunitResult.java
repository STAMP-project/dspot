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

    private Set<String> testsRun;
    private Map<String, Failure> testsFail;

    public JunitResult() {
        this.testsRun = new HashSet<>();
        this.testsFail = new HashMap<>();

    }

    void addTestRun(String testRun) {
        this.testsRun.add(testRun);
    }

    void addTestFail(Failure failure) {
        this.testsFail.put(failure.getDescription().getMethodName(), failure);
    }

    @Override
    public synchronized void testFinished(Description description) throws Exception {
        this.addTestRun(description.getMethodName());
    }

    @Override
    public synchronized void testFailure(Failure failure) throws Exception {
        this.addTestFail(failure);
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
        return new ArrayList<>(this.testsRun);
    }
}
