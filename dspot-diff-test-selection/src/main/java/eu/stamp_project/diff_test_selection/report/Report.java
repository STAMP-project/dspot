package eu.stamp_project.diff_test_selection.report;

import eu.stamp_project.diff_test_selection.coverage.DiffCoverage;

import java.util.Map;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/07/18
 */
public interface Report {

    /**
     * this method should out put the result in the given path
     * @param outputPath path out the report
     * @param testThatExecuteChanges map that associates full qualified name
     * @param coverage contains which line are modified and which are modified AND executed
     */
    void report(
            final String outputPath,
            final Map<String, Set<String>> testThatExecuteChanges,
            final DiffCoverage coverage);

}
