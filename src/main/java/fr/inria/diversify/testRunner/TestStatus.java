package fr.inria.diversify.testRunner;

import spoon.reflect.declaration.CtMethod;

import java.util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/12/16
 */
public class TestStatus {

    private Map<Boolean, List<CtMethod>> status;

    public TestStatus() {
        reset();
    }

    public void reset() {
        status = new HashMap<>();
        status.put(true, new ArrayList<>());
        status.put(false, new ArrayList<>());
    }

    public void updateTestStatus(Collection<CtMethod> newTests, JunitResult result) {
        List<String> runTests = result.runTests();
        List<String> failedTests = result.failureTests();
        newTests.stream()
                .filter(test -> runTests.contains(test.getSimpleName()))
                .forEach(test -> {
                    if (failedTests.contains(test.getSimpleName())) {
                        status.get(false).add(test);
                    } else {
                        status.get(true).add(test);
                    }
                });
    }

    public List<CtMethod> get(Boolean key) {
        return this.status.get(key);
    }

}
