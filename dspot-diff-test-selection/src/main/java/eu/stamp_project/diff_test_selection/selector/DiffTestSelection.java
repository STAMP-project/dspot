package eu.stamp_project.diff_test_selection.selector;

import eu.stamp_project.diff_test_selection.coverage.Coverage;
import eu.stamp_project.diff_test_selection.coverage.DiffCoverage;

import java.util.Map;
import java.util.Set;

public abstract class DiffTestSelection {

    protected final String pathToFirstVersion;
    protected final String pathToSecondVersion;
    protected final Coverage coverageV1;
    protected final String diff;
    protected final DiffCoverage coverage;

    public DiffTestSelection(String pathToFirstVersion, String pathToSecondVersion, Coverage coverageV1, String diff, DiffCoverage coverage) {
        this.pathToFirstVersion = pathToFirstVersion;
        this.pathToSecondVersion = pathToSecondVersion;
        this.coverageV1 = coverageV1;
        this.diff = diff;
        this.coverage = coverage;
    }

    public abstract Map<String, Set<String>> selectTests();

    public DiffCoverage getCoverage() {
        return this.coverage;
    }

}
