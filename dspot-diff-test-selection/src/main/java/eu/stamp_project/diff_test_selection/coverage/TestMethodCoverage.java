package eu.stamp_project.diff_test_selection.coverage;

import java.util.*;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * on 14/06/2021
 */
public class TestMethodCoverage {

    public final String testMethodName;

    public final Map<String, ClassCoverage> classCoverageList;

    public TestMethodCoverage(String testMethodName) {
        this.testMethodName = testMethodName;
        this.classCoverageList = new LinkedHashMap<>();
    }

    public void addCoverage(String className, int line, int hitCounts) {
        if (!this.classCoverageList.containsKey(className)) {
            this.classCoverageList.put(className, new ClassCoverage(className));
        }
        this.classCoverageList.get(className).addCoverage(line, hitCounts);
    }

    public Set<String> getClasses() {
        return this.classCoverageList.keySet();
    }

    public List<LineCoverage> getCoverageForClass(String className) {
        return this.classCoverageList.get(className).getCoverages();
    }

}
