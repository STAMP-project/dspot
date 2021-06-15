package eu.stamp_project.diff_test_selection.coverage;

import java.util.*;

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
        this.testMethodsCoverage = new LinkedHashMap<>();
    }

    public void addCoverage(String testMethodName, String className, int line, int hitCounts) {
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

}
