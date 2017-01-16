package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

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

    // TODO maybe the both methods can be merge @see PitRunner.run

    public static List<PitResult> parse(File fileResults, CtType testClass) {
        final List<PitResult> results = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new FileReader(fileResults))) {
            buffer.lines().forEach(line -> {
                String[] splittedLine = line.split(",");
                PitResult.State state;
                try {
                    state = PitResult.State.valueOf(splittedLine[5]);
                } catch (Exception e) {
                    state = PitResult.State.NO_COVERAGE;
                }
                String fullQualifiedNameMutantOperator = splittedLine[2];
                CtMethod methodTest;
                try {
                    String[] nameMethod = splittedLine[6].split("\\(")[0].split("\\.");
                    methodTest = "none".equals(nameMethod[nameMethod.length - 1]) ? null : (CtMethod) testClass.getMethodsByName(nameMethod[nameMethod.length - 1]).get(0);
                } catch (Exception e) {
                    methodTest = null;
                }
                int lineNumber = Integer.parseInt(splittedLine[4]);
                String location = splittedLine[3];
                results.add(new PitResult(state, fullQualifiedNameMutantOperator, methodTest, lineNumber, location));
            });
            Log.debug("Number Of Mutants generated : {}", results.size());
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<PitResult> parseAll(File fileResults, Factory factory) {
        final List<PitResult> results = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new FileReader(fileResults))) {
            buffer.lines().forEach(line -> {
                String[] splittedLine = line.split(",");
                PitResult.State state = PitResult.State.valueOf(splittedLine[5]);
                String fullQualifiedNameMutantOperator = splittedLine[2];
                CtMethod methodTest;
                if ("none".equals(splittedLine[6])) {
                    methodTest = null;
                } else {
                    CtClass testClass = factory.Class().get(splittedLine[6].split("\\(")[1].substring(0, splittedLine[6].split("\\(")[1].length() - 1));
                    if (testClass == null) {
                        Log.error("{} not found", splittedLine[6].split("\\(")[1].substring(0, splittedLine[6].split("\\(")[1].length() - 1));
                        throw new RuntimeException();
                    }
                    String [] nameMethod = splittedLine[6].split("\\(")[0].split("\\.");
                    methodTest = (CtMethod) testClass.getMethodsByName(nameMethod[nameMethod.length-1]).get(0);
                }
                int lineNumber = Integer.parseInt(splittedLine[4]);
                String location = splittedLine[3];
                results.add(new PitResult(state, fullQualifiedNameMutantOperator, methodTest, lineNumber, location));
            });
            Log.debug("Number Of Mutants generated : {}", results.size());
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
