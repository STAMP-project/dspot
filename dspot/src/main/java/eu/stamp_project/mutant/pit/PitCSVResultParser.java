package eu.stamp_project.mutant.pit;

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
public class PitCSVResultParser extends AbstractParser {

    public PitCSVResultParser() {
        super("/mutations.csv");
    }

    public List<AbstractPitResult> parse(File fileResults) {
        final List<AbstractPitResult> results = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new FileReader(fileResults))) {
            buffer.lines().forEach(line -> {
                String[] splittedLines = line.split(",");
                if (splittedLines.length == 7) {
                    PitCSVResult.State state;
                    try {
                        state = PitCSVResult.State.valueOf(splittedLines[5]);
                    } catch (Exception e) {
                        state = PitCSVResult.State.NO_COVERAGE;
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
                    results.add(new PitCSVResult(fullQualifiedNameOfMutatedClass, state, fullQualifiedNameMutantOperator, fullQualifiedNameMethod, fullQualifiedNameClass, lineNumber, location));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
