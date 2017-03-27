package fr.inria.diversify.mutant.pit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class PitResultParser {

    public static List<PitResult> parse(File fileResults) {
        final List<PitResult> results = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new FileReader(fileResults))) {
            buffer.lines().forEach(line -> {
                String[] splittedLines = line.split(",");
                PitResult.State state;
                try {
                    state = PitResult.State.valueOf(splittedLines[5]);
                } catch (Exception e) {
                    state = PitResult.State.NO_COVERAGE;
                }
                String fullQualifiedNameMutantOperator = splittedLines[2];
                String fullQualifiedNameMethod;
                String fullQualifiedNameClass;
                if ("none".equals(splittedLines[6])) {
                    fullQualifiedNameMethod = "none";
                    fullQualifiedNameClass = "none";
                } else {
                    final String[] nameOfTheKiller = splittedLines[6].split("\\(");
                    fullQualifiedNameMethod = nameOfTheKiller[0];
                    fullQualifiedNameClass = nameOfTheKiller[1].substring(0, nameOfTheKiller[1].length() - 1);
                }
                int lineNumber = Integer.parseInt(splittedLines[4]);
                String location = splittedLines[3];
                results.add(new PitResult(state, fullQualifiedNameMutantOperator, fullQualifiedNameMethod, fullQualifiedNameClass, lineNumber, location));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
