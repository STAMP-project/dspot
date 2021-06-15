package eu.stamp_project.diff_test_selection.coverage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * on 14/06/2021
 */
public class ClassCoverage {

    public final String className;

    public final List<LineCoverage> coverages;

    public ClassCoverage(String className) {
        this.className = className;
        this.coverages = new ArrayList<>();
    }

    public void addCoverage(int line, int hitCounts) {
        this.coverages.add(new LineCoverage(line, hitCounts));
    }

    public List<LineCoverage> getCoverages() {
        return this.coverages;
    }

    public boolean contains(int line) {
        return this.coverages.stream().anyMatch(lineCoverage -> lineCoverage.line == line);
    }
}
