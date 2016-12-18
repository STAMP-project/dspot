package fr.inria.diversify.dspot;

import fr.inria.diversify.log.LogReader;
import fr.inria.diversify.log.TestCoverageParser;
import fr.inria.diversify.log.TestGraphReader;
import fr.inria.diversify.log.graph.Graph;
import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.util.FileUtils;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 03/12/15
 * Time: 14:09
 */
public class TestSelector {
    protected File logDir;
    protected Map<String, Integer> testAges;
    protected List<Coverage> branchCoverage;
    protected List<Graph> graphCoverage;
    protected int maxNumberOfTest;

    public TestSelector(File logDir, int maxNumberOfTest) {
        this.logDir = logDir;
        this.maxNumberOfTest = maxNumberOfTest;
    }

    protected void init() throws IOException {
        deleteLogFile();
        testAges = new HashMap<>();
        branchCoverage = null;
    }

    protected void updateLogInfo() throws IOException {
        LogReader logReader = new LogReader(logDir.getAbsolutePath());
        TestCoverageParser coverageParser = new TestCoverageParser();
        TestGraphReader graphReader = new TestGraphReader();

        logReader.addParser(graphReader);
        logReader.addParser(coverageParser);
        logReader.readLogs();

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

        deleteLogFile();
    }

    protected Collection<CtMethod> selectTestToAmp(Collection<CtMethod> oldTests, Collection<CtMethod> newTests) {
        Map<CtMethod, Set<String>> selectedTest = new HashMap<>();
        for (CtMethod test : newTests) {
            Set<String> tc = getTestCoverageFor(test);
            if (!tc.isEmpty()) {
                Set<String> parentTc = getParentTestCoverageFor(test);
                if (!parentTc.isEmpty()) {
                    selectedTest.put(test, new HashSet<>());
                } else {
                    if (!parentTc.containsAll(tc)) {
                        selectedTest.put(test, diff(tc, parentTc));
                    }
                }
            }
        }
        Set<CtMethod> mths = new HashSet<>();
        if (selectedTest.size() > maxNumberOfTest) {
            mths.addAll(reduceSelectedTest(selectedTest));
        } else {
            mths.addAll(selectedTest.keySet());
        }
        List<CtMethod> oldMths = new ArrayList<>();
        for (CtMethod test : oldTests) {
            String testName = test.getSimpleName();
            if (!testAges.containsKey(testName)) {
                testAges.put(testName, getAgesFor(test));
            }
            if (testAges.get(testName) > 0) {
                oldMths.add(test);
            }
        }

        Random r = new Random();
        while (oldMths.size() > maxNumberOfTest) {
            oldMths.remove(r.nextInt(oldMths.size()));
        }
        for (CtMethod oltMth : oldMths) {
            String testName = oltMth.getSimpleName();
            testAges.put(testName, testAges.get(testName) - 1);
        }
        return mths;
    }

    protected Integer getAgesFor(CtMethod test) {
        String testName = test.getSimpleName();
        if (testName.contains("_cf")) {
            return 2;
        }
        if (!AmplificationHelper.getAmpTestToParent().containsKey(test)) {
            return 3;
        }
        return 0;
    }

    public Collection<CtMethod> selectedAmplifiedTests(Collection<CtMethod> tests) {
        Map<CtMethod, Set<String>> amplifiedTests = new HashMap<>();
        for (CtMethod test : tests) {
            Set<String> tc = getTestCoverageFor(test);
            Set<String> parentTc = getParentTestCoverageFor(test);
            if (!tc.isEmpty() && !parentTc.containsAll(tc)) {
                amplifiedTests.put(test, diff(tc, parentTc));
            }
        }
        return reduceSelectedTest(amplifiedTests);
    }

    protected Collection<CtMethod> reduceSelectedTest(Map<CtMethod, Set<String>> selected) {
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

    protected Set<String> getTestCoverageFor(CtMethod ampTest) {
        return getCoverageFor(ampTest.getSimpleName());
    }

    protected CtMethod getParent(CtMethod test) {
        return AmplificationHelper.getAmpTestToParent().get(test);
    }

    protected Set<String> getParentTestCoverageFor(CtMethod mth) {
        CtMethod parent = getParent(mth);
        if (parent != null) {
            String parentName = parent.getSimpleName();
            if (parentName != null) {
                return getCoverageFor(parentName);
            }
        }
        return new HashSet<>();
    }

    protected Set<String> getCoverageFor(String mthName) {
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

    protected void deleteLogFile() throws IOException {
        for (File file : logDir.listFiles()) {
            if (!file.getName().equals("info")) {
                FileUtils.forceDelete(file);
            }
        }
    }

    protected Set<String> diff(Set<String> set1, Set<String> set2) {
        Set<String> diff = set2.stream()
                .filter(branch -> !branch.contains(branch))
                .collect(Collectors.toSet());
        set1.stream()
                .filter(branch -> !set2.contains(branch))
                .forEach(branch -> diff.add(branch));

        return diff;
    }

    public Coverage getGlobalCoverage() {
        Coverage coverage = new Coverage("global");

        for (Coverage tc : branchCoverage) {
            coverage.merge(tc);
        }

        return coverage;
    }

    public List<Coverage> getCoverage() {
        return branchCoverage;
    }
}
