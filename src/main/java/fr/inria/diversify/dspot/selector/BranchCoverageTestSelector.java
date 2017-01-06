package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.log.LogReader;
import fr.inria.diversify.log.TestCoverageParser;
import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.runner.InputConfiguration;
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
 * on 1/5/17
 */
public class BranchCoverageTestSelector implements TestSelector {

    private File logDir;

    private Map<String, Integer> testAges;

    private List<Coverage> branchCoverage;

    private int maxNumberOfTest;

    private List<CtMethod> oldTests;

    public BranchCoverageTestSelector(int maxNumberOfTest) {
        this.maxNumberOfTest = maxNumberOfTest;
        this.oldTests = new ArrayList<>();
    }

    @Override
    public void init(InputConfiguration configuration) {
        this.logDir = new File(configuration.getInputProgram().getProgramDir() + "/log");
    }

    @Override
    public void reset() {
        deleteLogFile();
        this.testAges = new HashMap<>();
        this.branchCoverage = null;
        this.oldTests.clear();
    }

    @Override
    public List<CtMethod> selectToAmplify(List<CtMethod> testsToBeAmplified) {
        if (this.oldTests.isEmpty()) {
            this.oldTests.addAll(testsToBeAmplified);
        }
        Map<CtMethod, Set<String>> selectedTest = new HashMap<>();
        for (CtMethod test : testsToBeAmplified) {
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
        List<CtMethod> testMethodsSelected = new ArrayList<>();
        if (selectedTest.size() > maxNumberOfTest) {
            testMethodsSelected.addAll(reduceSelectedTest(selectedTest));
        } else {
            testMethodsSelected.addAll(selectedTest.keySet());
        }
        updateOldMethods();
        return testMethodsSelected;
    }

    @Override
    public List<CtMethod> selectToKeep(List<CtMethod> amplifiedTestToBeKept) {
        Map<CtMethod, Set<String>> amplifiedTests = new HashMap<>();
        for (CtMethod test : amplifiedTestToBeKept) {
            Set<String> tc = getTestCoverageFor(test);
            Set<String> parentTc = getParentTestCoverageFor(test);
            if (!tc.isEmpty() && !parentTc.containsAll(tc)) {
                amplifiedTests.put(test, diff(tc, parentTc));
            }
        }
        return reduceSelectedTest(amplifiedTests);
    }

    @Override
    public void update() {
        LogReader logReader;
        try {
            logReader = new LogReader(logDir.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TestCoverageParser coverageParser = new TestCoverageParser();
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
        deleteLogFile();
    }

    private void deleteLogFile() {
        for (File file : logDir.listFiles()) {
            if (!file.getName().equals("info")) {
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException e) {
                    Log.warn("Could not delete {}", file);
                }
            }
        }
    }

    private void updateOldMethods() {
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
            final Integer minAge = testAges.get(oldMethods.stream().min((m1, m2) -> testAges.get(m1.getSimpleName()) - testAges.get(m2.getSimpleName())).get().getSimpleName());
            Optional<CtMethod> oldestMethod;
            while ((oldestMethod = oldMethods.stream().filter(method -> testAges.get(method.getSimpleName()).equals(minAge)).findAny()).isPresent()) {
                oldMethods.remove(oldestMethod.get());
            }
        }
        oldMethods.forEach(method -> testAges.put(method.getSimpleName(), testAges.get(method.getSimpleName()) - 1));
    }

    private int getAgesFor(CtMethod test) {
        String testName = test.getSimpleName();
        if (testName.contains("_cf")) {
            return 2;
        }
        if (!AmplificationHelper.getAmpTestToParent().containsKey(test)) {
            return 3;
        }
        return 0;
    }

    private Set<String> getTestCoverageFor(CtMethod ampTest) {
        return getCoverageFor(ampTest.getSimpleName());
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

    private Set<String> getCoverageFor(String mthName) {
        Set<String> set = new LinkedHashSet<>();
        branchCoverage.stream()
                .filter(c -> c.getName().endsWith(mthName))
                .findFirst()
                .ifPresent(coverage -> set.addAll(coverage.getCoverageBranch()));
        return set;
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

    @Override
    public void report() {
        //TODO
    }

}
