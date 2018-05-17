package eu.stamp_project.mutant.pit;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class PitResultParser {

    public static List<PitResult> parseAndDelete(String pathToDirectoryResults) {
        if (!new File(pathToDirectoryResults).exists()) {
            return null;
        }
        final File[] files = new File(pathToDirectoryResults).listFiles();
        if (files == null) {
            return null;
        }
        File directoryReportPit = files[0];
        if (!directoryReportPit.exists()) {
            return null;
        }
        File fileResults = new File(directoryReportPit.getPath() + "/mutations.csv");
        final List<PitResult> results = PitResultParser.parse(fileResults);
        try {
            FileUtils.deleteDirectory(directoryReportPit);
        } catch (IOException e) {
            // ignored
        }
        return results;
    }

    public static List<PitResult> parse(File fileResults) {
        final List<PitResult> results = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new FileReader(fileResults))) {
            buffer.lines().forEach(line -> {
                String[] splittedLines = line.split(",");
                if (splittedLines.length == 7) {
                    PitResult.State state;
                    try {
                        state = PitResult.State.valueOf(splittedLines[5]);
                    } catch (Exception e) {
                        state = PitResult.State.NO_COVERAGE;
                    }
                    String fullQualifiedNameOfMutatedClass = splittedLines[1];
                    String fullQualifiedNameMutantOperator = splittedLines[2];
                    String fullQualifiedNameMethod;
                    String fullQualifiedNameClass;
                    if ("none".equals(splittedLines[6])) {
                        fullQualifiedNameMethod = "none";
                        fullQualifiedNameClass = "none";
                    } else {
                        final String[] nameOfTheKiller = splittedLines[6].split("\\(");
                        if (nameOfTheKiller.length > 1) {
                            fullQualifiedNameMethod = nameOfTheKiller[0];
                            fullQualifiedNameClass = nameOfTheKiller[1].substring(0, nameOfTheKiller[1].length() - 1);
                        } else {
                            fullQualifiedNameMethod = "none";
                            fullQualifiedNameClass = nameOfTheKiller[0].substring(0, nameOfTheKiller[0].length() / 2);
                        }
                    }
                    int lineNumber = Integer.parseInt(splittedLines[4]);
                    String location = splittedLines[3];
                    results.add(new PitResult(fullQualifiedNameOfMutatedClass, state, fullQualifiedNameMutantOperator, fullQualifiedNameMethod, fullQualifiedNameClass, lineNumber, location));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
