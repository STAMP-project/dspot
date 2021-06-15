package eu.stamp_project.diff_test_selection.coverage;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * on 14/06/2021
 */
public class LineCoverage {

    public final int line;

    public final int hitCount;

    public LineCoverage(int line, int hitCount) {
        this.line = line;
        this.hitCount = hitCount;
    }
}
