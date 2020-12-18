package eu.stamp_project.diff_test_selection;

import eu.stamp_project.diff_test_selection.clover.CloverExecutor;
import eu.stamp_project.diff_test_selection.clover.CloverReader;
import eu.stamp_project.diff_test_selection.configuration.Configuration;
import eu.stamp_project.diff_test_selection.configuration.Options;
import eu.stamp_project.diff_test_selection.coverage.Coverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/02/19
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Main.run(Options.parse(args));
    }

    public static void run(Configuration configuration) {
        final Map<String, Map<String, Map<String, List<Integer>>>> coverage = getCoverage(configuration.pathToFirstVersion);
        final DiffTestSelection diffTestSelection = new DiffTestSelection(configuration, coverage);
        final Map<String, Set<String>> selectedTests = diffTestSelection.getTestThatExecuteChanges();
    }

    private static void enhancedRun(Configuration configuration) {
        final Map<String, Map<String, Map<String, List<Integer>>>> coverageV1 =
                getCoverage(configuration.pathToFirstVersion);
        final Map<String, Map<String, Map<String, List<Integer>>>> coverageV2 =
                getCoverage(configuration.pathToSecondVersion);
        new EnhancedDiffTestSelection(
                configuration.pathToFirstVersion,
                configuration.pathToSecondVersion,
                coverageV1,
                coverageV2,
                configuration.diff
        ).selectTests();
    }

    private static void output(Configuration configuration, Coverage coverage, Map<String, Set<String>> selectedTests) {
        LOGGER.info("Saving result in " + configuration.outputPath + " ...");
        configuration.reportFormat.instance.report(
                configuration.outputPath,
                selectedTests,
                coverage
        );
    }

    private static Map<String, Map<String, Map<String, List<Integer>>>> getCoverage(final String pathToFirstVersion) {
        LOGGER.info("Computing coverage for " + pathToFirstVersion);
        new CloverExecutor().instrumentAndRunTest(pathToFirstVersion);
        return new CloverReader().read(pathToFirstVersion);
    }
}
