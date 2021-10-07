package eu.stamp_project.diff_test_selection.coverage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * on 14/06/2021
 */
public class TestClassCoverage {

    public final String testClassName;

    public final Map<String, TestMethodCoverage> testMethodsCoverage;

    public TestClassCoverage(String testClassName) {
        this.testClassName = testClassName;
        this.testMethodsCoverage = new ConcurrentHashMap<>();
    }

    public synchronized void addCoverage(String testMethodName, String className, int line, int hitCounts) {
        if (!this.testMethodsCoverage.containsKey(testMethodName)) {
            this.testMethodsCoverage.put(testMethodName, new TestMethodCoverage(testMethodName));
        }
        this.testMethodsCoverage.get(testMethodName).addCoverage(className, line, hitCounts);
    }

    public Set<String> getTestMethods() {
        return this.testMethodsCoverage.keySet();
    }

    public Set<String> getClassesForTestMethodName(String testMethodName) {
        return this.testMethodsCoverage.get(testMethodName).getClasses();
    }

    public List<LineCoverage> getCoverageForTestMethodAndClassNames(String testMethodName, String className) {
        return this.testMethodsCoverage.get(testMethodName).getCoverageForClass(className);
    }

    public Map<String, ClassCoverage> getTestMethodCoverage(String testMethodName) {
        return this.testMethodsCoverage.get(testMethodName).classCoverageList;
    }

    public Map<String, Integer> getHitCountFromClassNameForLineForAll(String className, int line) {
        final Map<String, Integer> hitCountForLinePerTestMethodName = new HashMap<>();
        for (TestMethodCoverage testMethodCoverage : testMethodsCoverage.values()) {
            final int hitCountFromClassNameForLine = testMethodCoverage.getHitCountFromClassNameForLine(className, line);
            hitCountForLinePerTestMethodName.put(this.testClassName + "#" + testMethodCoverage.testMethodName, hitCountFromClassNameForLine);
        }
        return hitCountForLinePerTestMethodName;
    }

    public void merge(TestClassCoverage that) {
        for (String testMethodName : that.testMethodsCoverage.keySet()) {
            if (!this.testMethodsCoverage.containsKey(testMethodName)) {
                this.testMethodsCoverage.put(testMethodName, that.testMethodsCoverage.get(testMethodName));
            } else {
                this.testMethodsCoverage.get(testMethodName).merge(that.testMethodsCoverage.get(testMethodName));
            }
        }
    }

    @Override
    public String toString() {
        return "TestClassCoverage{" +
                "testClassName='" + testClassName + '\'' +
                ", testMethodsCoverage=" + testMethodsCoverage +
                '}';
    }
}
