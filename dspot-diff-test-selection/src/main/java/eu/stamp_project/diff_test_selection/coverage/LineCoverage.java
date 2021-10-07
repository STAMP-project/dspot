package eu.stamp_project.diff_test_selection.coverage;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * on 14/06/2021
 */
public class LineCoverage {

    public final int line;

    private int hitCount;

    public LineCoverage(int line, int hitCount) {
        this.line = line;
        this.hitCount = hitCount;
    }

    public int getHitCount() {
        return this.hitCount;
    }

    public void merge(LineCoverage that) {
        this.hitCount += that.hitCount;
    }

    @Override
    public String toString() {
        return "LineCoverage{" +
                "line=" + line +
                ", hitCount=" + hitCount +
                '}';
    }
}
