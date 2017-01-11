package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

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

    public static List<PitResult> parse(File fileResults, CtType testClass) {
        final List<PitResult> results = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new FileReader(fileResults))) {
            buffer.lines().forEach(line -> {
                String[] splittedLine = line.split(",");
                PitResult.State state = PitResult.State.valueOf(splittedLine[5]);
                String fullQualifiedNameMutantOperator = splittedLine[2];
                String [] nameMethod = splittedLine[6].split("\\(")[0].split("\\.");
                CtMethod methodTest = "none".equals(nameMethod[nameMethod.length-1]) ? null : (CtMethod) testClass.getMethodsByName(nameMethod[nameMethod.length-1]).get(0);
                int lineNumber = Integer.parseInt(splittedLine[4]);
                String location = splittedLine[3];
                results.add(new PitResult(state, fullQualifiedNameMutantOperator, methodTest, lineNumber, location));
            });
        } catch (IOException e) {
            Log.warn("Error during reading report file of pits.");
        }
        return results;
    }


}
