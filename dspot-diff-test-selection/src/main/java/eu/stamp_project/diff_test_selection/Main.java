package eu.stamp_project.diff_test_selection;

import eu.stamp_project.diff_test_selection.clover.CloverExecutor;
import eu.stamp_project.diff_test_selection.clover.CloverReader;
import eu.stamp_project.diff_test_selection.configuration.Configuration;
import eu.stamp_project.diff_test_selection.configuration.Options;
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
        final Map<String, Set<String>> testThatExecuteChanges = diffTestSelection.getTestThatExecuteChanges();
        LOGGER.info("Saving result in " + configuration.outputPath + " ...");
        configuration.reportFormat.instance.report(
                configuration.outputPath,
                testThatExecuteChanges,
                diffTestSelection.getCoverage()
        );
    }

    private static Map<String, Map<String, Map<String, List<Integer>>>> getCoverage(final String pathToFirstVersion) {
        //if (!skipCoverage) {
            LOGGER.info("Computing coverage for " + pathToFirstVersion);
            new CloverExecutor().instrumentAndRunTest(pathToFirstVersion);
        //}
        return new CloverReader().read(pathToFirstVersion);
    }
}
