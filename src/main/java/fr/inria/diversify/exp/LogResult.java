package fr.inria.diversify.exp;

import fr.inria.diversify.log.branch.Coverage;
import spoon.reflect.declaration.CtMethod;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 17/02/16
 * Time: 10:25
 */
public class LogResult {
    protected static String dir;
    protected static FileWriter csv;
    protected static FileWriter failuresLog;

    protected static Map<String, Set<String>> branchByMethod = new HashMap<>();

    protected static Set<String> testOriginal = new HashSet<>();
    protected static Set<String> testAmp = new HashSet<>();

    protected static Set<String> branchOriginal = new HashSet<>();
    protected static Set<String> branchAmp = new HashSet<>();

    public LogResult(String directory) throws IOException {
        dir = directory;

        csv = new FileWriter(dir +"/result.csv");
        csv.write("MutantId;TCOriginal;TCAmp;TCKillOriginal;TCKillAmp;BranchCovOriginal;BranchCovAmp;NewBranchCovAmp\n");

        failuresLog = new FileWriter(dir + "/failures");
    }

    public static void log(int mutantId, List<String> triggerTests, List<String> failures) throws Exception {
        csv.write(mutantId + ";");

        csv.write(testOriginal.size() + ";");
        testAmp.removeAll(testOriginal);
        csv.write(testAmp.size() + ";");

        csv.write(triggerTests.size() + ";");
        if(failures != null) {
            csv.write(failures.size() + ";");
        } else {
            csv.write("-1;");
        }
        csv.write(branchOriginal.size() + ";");
        csv.write(branchAmp.size() + ";");
        branchAmp.removeAll(branchOriginal);
        csv.write(branchAmp.size() + "\n");

        csv.flush();

        logFailure(mutantId, failures);
        logBranchReport(mutantId);
        reset();
    }

    public static void close() throws IOException {
        csv.close();
        failuresLog.close();
    }

    public static void addCoverage(List<Coverage> coverage, Collection<CtMethod> tests, boolean original) throws IOException, InterruptedException {
        tests.stream()
                .filter(test -> getTestCoverageFor(coverage, test) != null)
                .forEach(test -> branchByMethod.put(test.getSimpleName(),
                        getTestCoverageFor(coverage, test).getCoverageBranch()));

        Set<String> branch = tests.stream()
                .map(test -> test.getSimpleName())
                .flatMap(testName -> branchByMethod.get(testName).stream())
                .collect(Collectors.toSet());

        if(original) {
            testOriginal.addAll(tests.stream().map(test -> test.getSimpleName()).collect(Collectors.toList()));
            branchOriginal.addAll(branch);
        } else {
            testAmp.addAll(tests.stream().map(test -> test.getSimpleName()).collect(Collectors.toList()));
            branchAmp.addAll(branch);
        }
    }

    protected static Coverage getTestCoverageFor(List<Coverage> coverage, CtMethod test) {
        String testName = test.getSimpleName();

        return coverage.stream()
                .filter(c -> c.getName().endsWith(testName))
                .findFirst()
                .orElse(null);
    }

    protected static void logFailure(int mutantId, List<String> failures) throws IOException {
        if(failures != null) {
            if(!failures.isEmpty()) {
                failuresLog.write("mutant "+ mutantId +": " + failures.size() +" tests failed\n");
                for(String failure : failures) {
                    failuresLog.write("\t"+failure+ "\n");
                }
            } else {
                failuresLog.write("mutant "+ mutantId + ": all tests green\n");
            }
        } else {
            failuresLog.write("mutant " + mutantId + ": failing tests on correct version\n");
        }
        failuresLog.flush();
    }

    protected static void logBranchReport(int mutantId) throws IOException {
        FileWriter report = new FileWriter(dir +"/branch_" + mutantId);

        for(String test : branchByMethod.keySet()) {
            report.write(test + ":");
            report.write(branchByMethod.get(test).size() + ", ");
            for(String branch : branchByMethod.get(test)) {
                report.write("\t" + branch + "\n");
            }
            report.write("\n");
        }
        report.close();
    }

    protected static void reset() {
        testOriginal.clear();
        testAmp.clear();
        branchOriginal.clear();
        branchAmp.clear();

        branchByMethod.clear();
    }
}
