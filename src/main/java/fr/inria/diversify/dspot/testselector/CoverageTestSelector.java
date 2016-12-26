package fr.inria.diversify.dspot.testselector;

import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.log.LogReader;
import fr.inria.diversify.log.TestCoverageParser;
import fr.inria.diversify.log.TestGraphReader;
import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.log.graph.Graph;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/13/16
 */
public class CoverageTestSelector implements TestSelector {

    private File logDir;
    private Map<String, Integer> testAges;
    private List<Coverage> branchCoverage;
    private List<Graph> graphCoverage;
    private int maxNumberOfTest;

    private List<CtMethod> oldTests;

    public CoverageTestSelector(File logDir, int maxNumberOfTest) {
        this.logDir = logDir;
        this.maxNumberOfTest = maxNumberOfTest;
        this.oldTests = new ArrayList<>();
    }

    @Override
    public void init() {
        deleteLogFile();
        this.testAges = new HashMap<>();
        this.branchCoverage = null;
    }

    @Override
    public List<CtMethod> select(List<CtMethod> methodTest) {
        this.oldTests.addAll(methodTest);
        Map<CtMethod, Set<String>> selectedTest = new HashMap<>();
        for (CtMethod test : methodTest) {
            Set<String> tc = getTestCoverageFor(test);
            if (!tc.isEmpty()) {
                Set<String> parentTc = getParentTestCoverageFor(test);
                if (!parentTc.isEmpty()) {
                    selectedTest.put(test, new HashSet<>());
                } else if (!parentTc.containsAll(tc)) {
                    selectedTest.put(test, diff(tc, parentTc));
                }
            }
        }
        if (selectedTest.size() > maxNumberOfTest) {
            return reduceSelectedTest(selectedTest);
        } else {
            return new ArrayList<>(selectedTest.keySet());
        }
    }

    @Override
    public void update() {
        discardTooOldMethodTest();
        TestCoverageParser coverageParser = new TestCoverageParser();
        TestGraphReader graphReader = new TestGraphReader();
        try {
            LogReader logReader = new LogReader(logDir.getAbsolutePath());
            logReader.addParser(graphReader);
            logReader.addParser(coverageParser);
            logReader.readLogs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateBranchCoverage(coverageParser);
        updateGraphCoverage(graphReader);
        deleteLogFile();
    }

    private void updateGraphCoverage(TestGraphReader graphReader) {
        if (graphCoverage == null) {
            graphCoverage = graphReader.getResult();
        } else {
            for (Graph coverage : graphReader.getResult()) {
                Graph previous = graphCoverage.stream()
                        .filter(ac -> ac.getName().equals(coverage.getName()))
                        .findFirst()
                        .orElse(null);
                if (previous != null) {
                    graphCoverage.remove(previous);
                }
                graphCoverage.add(coverage);
            }
        }
    }

    private void updateBranchCoverage(TestCoverageParser coverageParser) {
        if (branchCoverage == null) {
            branchCoverage = coverageParser.getResult();
        } else {
            for (Coverage coverage : coverageParser.getResult()) {
                Coverage previous = branchCoverage.stream()
                        .filter(ac -> ac.getName().equals(coverage.getName()))
                        .findFirst()
                        .orElse(null);
                if (previous != null) {
                    branchCoverage.remove(previous);
                }
                branchCoverage.add(coverage);
            }
        }
    }

    private void discardTooOldMethodTest() {
        List<CtMethod> oldMethods = new ArrayList<>();
        for (CtMethod test : oldTests) {
            String testName = test.getSimpleName();
            if (!testAges.containsKey(testName)) {
                testAges.put(testName, getAgesFor(test));
            }
            if (testAges.get(testName) > 0) {
                oldMethods.add(test);
            }
        }

        while (oldMethods.size() > maxNumberOfTest) {
            oldMethods.remove(AmplificationHelper.getRandom().nextInt(oldMethods.size()));
        }

        for (CtMethod oldMethod : oldMethods) {
            String testName = oldMethod.getSimpleName();
            testAges.put(testName, testAges.get(testName) - 1);
        }
    }

    private Integer getAgesFor(CtMethod test) {
        if (test.getSimpleName().contains("_cf")) {
            return 2;
        } else if (!AmplificationHelper.getAmpTestToParent().containsKey(test)) {
            return 3;
        } else {
            return 0;
        }
    }

    private List<CtMethod> reduceSelectedTest(Map<CtMethod, Set<String>> selected) {
        Map<Set<String>, List<CtMethod>> map = selected.keySet().stream()
                .collect(Collectors.groupingBy(mth -> selected.get(mth)));

        List<Set<String>> sortedKey = map.keySet().stream()
                .sorted((l1, l2) -> Integer.compare(l2.size(), l1.size()))
                .collect(Collectors.toList());

        List<CtMethod> methods = new ArrayList<>();
        while (!sortedKey.isEmpty()) {
            Set<String> key = new HashSet<>(sortedKey.remove(0));

            if (map.containsKey(key)) {
                methods.add(map.get(key).stream().findAny().get());

            }
            sortedKey = sortedKey.stream()
                    .map(k -> {
                        k.removeAll(key);
                        return k;
                    })
                    .filter(k -> !k.isEmpty())
                    .sorted((l1, l2) -> Integer.compare(l2.size(), l1.size()))
                    .collect(Collectors.toList());

            map.keySet().forEach(set -> set.removeAll(key));
        }
        return methods;
    }

    private Set<String> getTestCoverageFor(CtMethod ampTest) {
        return getCoverageFor(ampTest.getSimpleName());
    }

    private Set<String> getCoverageFor(String mthName) {
        Set<String> set = new HashSet<>();

        branchCoverage.stream()
                .filter(c -> c.getName().endsWith(mthName))
                .findFirst()
                .ifPresent(coverage -> set.addAll(coverage.getCoverageBranch()));

        graphCoverage.stream()
                .filter(c -> c.getName().endsWith(mthName))
                .findFirst()
                .ifPresent(graph -> set.addAll(graph.getEdges()));

        return set;
    }

    private CtMethod getParent(CtMethod test) {
        return AmplificationHelper.getAmpTestToParent().get(test);
    }

    private Set<String> getParentTestCoverageFor(CtMethod mth) {
        CtMethod parent = getParent(mth);
        if (parent != null) {
            String parentName = parent.getSimpleName();
            if (parentName != null) {
                return getCoverageFor(parentName);
            }
        }
        return new HashSet<>();
    }

    private Set<String> diff(Set<String> set1, Set<String> set2) {
        Set<String> diff = set2.stream()
                .filter(branch -> !branch.contains(branch))
                .collect(Collectors.toSet());
        set1.stream()
                .filter(branch -> !set2.contains(branch))
                .forEach(branch -> diff.add(branch));
        return diff;
    }

    private void deleteLogFile() {
        if (logDir != null && logDir.listFiles() != null) {
            for (File file : logDir.listFiles()) {
                try {
                    if (!file.getName().equals("info")) {
                        FileUtils.forceDelete(file);
                    }
                } catch (IOException e) {
                    Log.warn("Error during cleaning the log directory: " + file.getName());
                }
            }
        }
    }
}
