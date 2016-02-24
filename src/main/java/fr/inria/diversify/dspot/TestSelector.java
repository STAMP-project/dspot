package fr.inria.diversify.dspot;

import fr.inria.diversify.dspot.amp.AbstractAmp;
import fr.inria.diversify.coverage.branch.Coverage;
import fr.inria.diversify.coverage.branch.CoverageReader;
import fr.inria.diversify.runner.InputProgram;
import org.apache.commons.io.FileUtils;
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
    protected InputProgram inputProgram;
    protected Map<String, Integer> testAges;
    protected List<Coverage> branchCoverage;



    protected int maxNumberOfTest;

    public TestSelector(InputProgram inputProgram,  int maxNumberOfTest) {
        this.inputProgram = inputProgram;
        this.maxNumberOfTest = maxNumberOfTest;
    }

    protected void init() throws IOException {
        deleteLogFile();
        testAges = new HashMap<>();
        branchCoverage = null;
    }

    protected void updateLogInfo() throws IOException {
        try {
            CoverageReader reader = new CoverageReader(inputProgram.getProgramDir() + "/log");
            if (branchCoverage == null) {
                branchCoverage = reader.loadTest();
            } else {
                for (Coverage coverage : reader.loadTest()) {
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
        } catch (Throwable e) {
            e.printStackTrace();
            System.gc();
        }
        deleteLogFile();
    }

    protected Collection<CtMethod> selectTestToAmp(Collection<CtMethod> oldTests, Collection<CtMethod> newTests) {
        Map<CtMethod, Set<String>> selectedTest = new HashMap<>();
        for (CtMethod test : newTests) {
            Coverage tc = getTestCoverageFor(test);
            if(tc != null) {
                Coverage parentTc = getParentTestCoverageFor(test);
                if (parentTc == null) {
                    selectedTest.put(test, new HashSet<>());
                } else {
                    if (!parentTc.containsAllBranch(tc)) {
                        selectedTest.put(test, tc.diff(parentTc));
                    }
                }
            }
        }
        Set<CtMethod> mths = new HashSet<>();
        if(selectedTest.size() > maxNumberOfTest) {
            mths.addAll(reduceSelectedTest(selectedTest));
        } else {
            mths.addAll(selectedTest.keySet());
//            return selectedTest.keySet();
        }
        List<CtMethod> oldMths = new ArrayList<>();
        for (CtMethod test : oldTests) {
            String testName = test.getSimpleName();
            if (!testAges.containsKey(testName)) {
                testAges.put(testName, getAgesFor(test));
            }
            if (testAges.get(testName) > 0) {
//                testAges.put(testName, testAges.get(testName) - 1);
                oldMths.add(test);
            }
        }

        Random r = new Random();
        while(oldMths.size() > maxNumberOfTest) {
            oldMths.remove(r.nextInt(oldMths.size()));
        }
        for(CtMethod oltMth : oldMths) {
            String testName = oltMth.getSimpleName();
            testAges.put(testName, testAges.get(testName) - 1);
        }

        return mths;
    }

    protected Integer getAgesFor(CtMethod test) {
        String testName = test.getSimpleName();
        if(testName.contains("_cf")) {
            return 2;
        }
        if(!AbstractAmp.getAmpTestToParent().containsKey(test)) {
            return 3;
        }
        return 0;
    }


    public Collection<CtMethod> selectedAmplifiedTests(Collection<CtMethod> tests) {
        Map<CtMethod, Set<String>> amplifiedTests = new HashMap<>();
        for (CtMethod test : tests) {
            Coverage tc = getTestCoverageFor(test);
            Coverage parentTc = getParentTestCoverageFor(test);
            if (tc != null && parentTc != null) {
                if (!tc.getCoverageBranch().isEmpty()) {
                    if (!parentTc.containsAllBranch(tc)) {
                        amplifiedTests.put(test, tc.diff(parentTc));
                    }
                }
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
        while(!sortedKey.isEmpty()) {
            Set<String> key = new HashSet<>(sortedKey.remove(0));

            if(map.containsKey(key)) {
                methods.add(map.get(key).stream().findAny().get());

            }
            sortedKey = sortedKey.stream()
                    .map(k -> {k.removeAll(key); return k;})
                    .filter(k -> !k.isEmpty())
                    .sorted((l1, l2) -> Integer.compare(l2.size(), l1.size()))
                    .collect(Collectors.toList());

            map.keySet().stream()
                    .forEach(set -> set.removeAll(key));
        }

        return methods;
    }

    protected Coverage getTestCoverageFor(CtMethod ampTest) {
        String testName = ampTest.getSimpleName();

        return branchCoverage.stream()
                .filter(c -> c.getName().endsWith(testName))
                .findFirst()
                .orElse(null);
    }

    protected CtMethod getParent(CtMethod test) {
        return AbstractAmp.getAmpTestToParent().get(test);
    }

    protected Coverage getParentTestCoverageFor(CtMethod mth) {
        CtMethod parent = getParent(mth);
        if(parent != null) {
            String parentName = parent.getSimpleName();
            if (parentName != null) {
                return branchCoverage.stream()
                        .filter(c -> c.getName().endsWith(parentName))
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
    }

    protected void deleteLogFile() throws IOException {
        File dir = new File(inputProgram.getProgramDir()+ "/log");
        for(File file : dir.listFiles()) {
            if(!file.getName().equals("info")) {
                FileUtils.forceDelete(file);
            }
        }
    }

    public Coverage getGlobalCoverage() {
        Coverage coverage = new Coverage("global");

        for(Coverage tc : branchCoverage) {
            coverage.merge(tc);
        }

        return coverage;
    }

    public List<Coverage> getCoverage() {
        return branchCoverage;
    }
}
