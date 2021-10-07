package eu.stamp_project.diff_test_selection.coverage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        this.classCoverageList = new ConcurrentHashMap<>();
    }

    public synchronized void addCoverage(String className, int line, int hitCounts) {
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

    public int getHitCountFromClassNameForLine(String className, int line) {
        if (this.classCoverageList.containsKey(className)) {
            return this.classCoverageList.get(className).getHitCountForLine(line);
        } else {
            return 0;
        }
    }

    public void merge(TestMethodCoverage that) {
        for (String className : that.classCoverageList.keySet()) {
            if (!this.classCoverageList.containsKey(className)) {
                this.classCoverageList.put(className, that.classCoverageList.get(className));
            } else {
                this.classCoverageList.get(className).merge(that.classCoverageList.get(className));
            }
        }
    }

    @Override
    public String toString() {
        return "TestMethodCoverage{" +
                "testMethodName='" + testMethodName + '\'' +
                ", classCoverageList=" + classCoverageList +
                '}';
    }
}
