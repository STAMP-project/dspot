package eu.stamp_project.diff_test_selection.selector;

import eu.stamp_project.diff_test_selection.coverage.Coverage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DiffTestSelection {

    protected final String pathToFirstVersion;
    protected final String pathToSecondVersion;
    protected final Map<String, Map<String, Map<String, List<Integer>>>> coverageV1;
    protected final String diff;
    protected final Coverage coverage;

    public DiffTestSelection(String pathToFirstVersion, String pathToSecondVersion, Map<String, Map<String, Map<String, List<Integer>>>> coverageV1, String diff, Coverage coverage) {
        this.pathToFirstVersion = pathToFirstVersion;
        this.pathToSecondVersion = pathToSecondVersion;
        this.coverageV1 = coverageV1;
        this.diff = diff;
        this.coverage = coverage;
    }

    public abstract Map<String, Set<String>> selectTests();

    public Coverage getCoverage() {
        return this.coverage;
    }

}
