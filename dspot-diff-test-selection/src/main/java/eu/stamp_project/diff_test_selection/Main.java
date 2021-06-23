package eu.stamp_project.diff_test_selection;

import eu.stamp_project.diff_test_selection.clover.CloverExecutor;
import eu.stamp_project.diff_test_selection.clover.CloverReader;
import eu.stamp_project.diff_test_selection.configuration.Configuration;
import eu.stamp_project.diff_test_selection.configuration.Options;
import eu.stamp_project.diff_test_selection.coverage.Coverage;
import eu.stamp_project.diff_test_selection.coverage.DiffCoverage;
import eu.stamp_project.diff_test_selection.selector.DiffTestSelection;
import eu.stamp_project.diff_test_selection.selector.DiffTestSelectionImpl;
import eu.stamp_project.diff_test_selection.selector.EnhancedDiffTestSelection;
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
        final Map<String, Set<String>> selectedTests;
        final DiffCoverage coverage = new DiffCoverage();
        if (configuration.enhanced) {
            LOGGER.info("Running in enhanced mode...");
            selectedTests = enhancedRun(configuration, coverage);
        } else {
            LOGGER.info("Running...");
            selectedTests = _run(configuration, coverage);
        }
        output(configuration, coverage, selectedTests);
    }

    public static Map<String, Set<String>> _run(Configuration configuration, DiffCoverage coverage) {
        final Coverage cloverCoverage = getCoverage(configuration.pathToFirstVersion);
        final DiffTestSelection diffTestSelectionImpl = new DiffTestSelectionImpl(
                configuration.pathToFirstVersion,
                configuration.pathToSecondVersion,
                cloverCoverage,
                configuration.diff,
                coverage
        );
        return diffTestSelectionImpl.selectTests();
    }

    private static Map<String, Set<String>> enhancedRun(Configuration configuration, DiffCoverage coverage) {
        final Coverage cloverCoverageV1 = getCoverage(configuration.pathToFirstVersion);
        final Coverage cloverCoverageV2 = getCoverage(configuration.pathToSecondVersion);
        return new EnhancedDiffTestSelection(
                configuration.pathToFirstVersion,
                configuration.pathToSecondVersion,
                cloverCoverageV1,
                configuration.diff,
                coverage,
                cloverCoverageV2
        ).selectTests();
    }

    private static void output(Configuration configuration, DiffCoverage coverage, Map<String, Set<String>> selectedTests) {
        LOGGER.info("Saving result in " + configuration.outputPath + " ...");
        configuration.reportFormat.instance.report(
                configuration.outputPath,
                selectedTests,
                coverage
        );
    }

    private static Coverage getCoverage(final String pathToFirstVersion) {
        LOGGER.info("Computing coverage for " + pathToFirstVersion);
        new CloverExecutor().instrumentAndRunTest(pathToFirstVersion);
        return new CloverReader().read(pathToFirstVersion);
    }
}
