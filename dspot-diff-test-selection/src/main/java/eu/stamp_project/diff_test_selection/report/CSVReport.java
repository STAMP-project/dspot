package eu.stamp_project.diff_test_selection.report;

import eu.stamp_project.diff_test_selection.coverage.Coverage;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/07/18
 */
public class CSVReport implements Report {

    private static final String SEMI_COLON = ";";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public void report(final Log log,
                       final String outputPath,
                       final Map<String, Set<String>> testThatExecuteChanges,
                       final Coverage coverage) {
        final File file = new File(outputPath);
        StringBuilder builder = new StringBuilder();
        String report = testThatExecuteChanges.keySet()
                .stream()
                .map(testClassName ->
                        testClassName + SEMI_COLON +
                                testThatExecuteChanges.get(testClassName)
                                        .stream()
                                        .collect(Collectors.joining(SEMI_COLON))
                ).collect(Collectors.joining(LINE_SEPARATOR));
        builder.append(builder);
        log.info(report);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(report);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reportCoverageOfDiff(log, outputPath, coverage);
    }

    private void reportCoverageOfDiff(final Log log,
                                      final String outputPath,
                                      final Coverage coverage) {
        final String output = outputPath.substring(0, outputPath.length() - ".csv".length()) + "_coverage.csv";
        log.info("Writing Coverage in " + output);
        final Map<String, Set<Integer>> executedLinePerQualifiedName = coverage.getExecutedLinePerQualifiedName();
        final Map<String, Set<Integer>> modifiedLinePerQualifiedName = coverage.getModifiedLinePerQualifiedName();
        final StringBuilder report = new StringBuilder();
        int executedTotal = 0;
        int modifiedTotal = 0;
        for (String fullQualifiedName : executedLinePerQualifiedName.keySet()) {
            final int executed = executedLinePerQualifiedName.get(fullQualifiedName).size();
            if (!modifiedLinePerQualifiedName.containsKey(fullQualifiedName)) {
                continue;
            }
            final int modified = modifiedLinePerQualifiedName.get(fullQualifiedName).size();
            executedTotal += executed;
            modifiedTotal += modified;
            final String line = fullQualifiedName + ";" + executed + ";" + modified + LINE_SEPARATOR;
            log.info(line);
            report.append(line);
        }
        final String line = "total;" + executedTotal + ";" + modifiedTotal + LINE_SEPARATOR;
        log.info(line);
        report.append(line);
        try {
            FileWriter writer = new FileWriter(output);
            writer.write(report.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
