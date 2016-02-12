package fr.inria.diversify.dspot;

import fr.inria.diversify.dspot.processor.AbstractAmp;
import fr.inria.diversify.profiling.coverage.Coverage;
import fr.inria.diversify.profiling.coverage.CoverageReader;
import fr.inria.diversify.profiling.coverage.TestCoverage;
import fr.inria.diversify.profiling.processor.test.AssertionRemover;
import fr.inria.diversify.profiling.processor.test.TestLoggingInstrumenter;
import fr.inria.diversify.runner.InputProgram;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

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
    protected List<TestCoverage> ampCoverage;
    protected String logger;
    protected  int maxNumberOfTest;

    public TestSelector(InputProgram inputProgram,  int maxNumberOfTest) {
        this.inputProgram = inputProgram;
        this.logger = "fr.inria.diversify.profiling.logger";
        this.maxNumberOfTest = maxNumberOfTest;
    }

    protected void init() throws IOException {
        deleteLogFile();
        testAges = new HashMap<>();
        ampCoverage = null;
    }

    protected void updateLogInfo() throws IOException {
        try {
            CoverageReader reader = new CoverageReader(inputProgram.getProgramDir() + "/log");
            if (ampCoverage == null) {
                ampCoverage = reader.loadTest();
            } else {
                for (TestCoverage coverage : reader.loadTest()) {
                    TestCoverage previous = ampCoverage.stream()
                            .filter(ac -> ac.getTestName().equals(coverage.getTestName()))
                            .findFirst()
                            .orElse(null);
                    if (previous != null) {
                        ampCoverage.remove(previous);
                    }
                    ampCoverage.add(coverage);
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
            for (TestCoverage tc : getTestCoverageFor(test)) {
                TestCoverage parentTc = getParentTestCoverageFor(test);
                if (parentTc == null) {
                    selectedTest.put(test, new HashSet<>());
                    break;
                } else {
                    if (!parentTc.containsAllBranch(tc)) {
                        selectedTest.put(test, tc.diff(parentTc));
                        break;
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
            for (TestCoverage tc : getTestCoverageFor(test)) {
                TestCoverage parentTc = getParentTestCoverageFor(test);
                if (parentTc != null && tc.containsAllBranch(parentTc) && !parentTc.containsAllBranch(tc)) {
                    amplifiedTests.put(test, tc.diff(parentTc));
                    break;
                }
                if(parentTc != null && !tc.containsAllBranch(parentTc) && !parentTc.containsAllBranch(tc)) {
                    amplifiedTests.put(test, tc.diff(parentTc));
                    break;
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

    protected List<TestCoverage> getTestCoverageFor(CtMethod ampTest) {
        String testName = ampTest.getSimpleName();

        return ampCoverage.stream()
                .filter(c -> c.getTestName().endsWith(testName))
                .collect(Collectors.toList());
    }

    protected CtMethod getParent(CtMethod test) {
        return AbstractAmp.getAmpTestToParent().get(test);
    }

    protected TestCoverage getParentTestCoverageFor(CtMethod mth) {
        CtMethod parent = getParent(mth);
        if(parent != null) {
            String parentName = parent.getSimpleName();
            if (parentName != null) {
                return ampCoverage.stream()
                        .filter(c -> c.getTestName().endsWith(parentName))
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
    }

    protected CtType buildClassWithLogger(CtType originalClass, CtMethod test) {
        List<CtMethod> tests = new ArrayList<>(1);
        tests.add(test);
        return buildClassWithLogger(originalClass, tests);
    }

    protected CtType buildClassWithLogger(CtType originalClass, Collection<CtMethod> tests) {
        CtType cloneClass = originalClass.getFactory().Core().clone(originalClass);
        cloneClass.setParent(originalClass.getParent());
        tests.stream()
                .map(test -> buildMethodWithLogger(cloneClass, test))
                .forEach(instruTest -> {
                    cloneClass.removeMethod(instruTest);
                    cloneClass.addMethod(instruTest);
                });
        return cloneClass;
    }

    protected CtMethod buildMethodWithLogger(CtType parentClass, CtMethod method) {
        CtMethod clone = cloneMethod(method);
        clone.setParent(parentClass);
        AssertionRemover testCase = new AssertionRemover(inputProgram.getAbsoluteTestSourceCodeDir(), false);
        testCase.setLogger(logger + ".Logger");
        testCase.setFactory(inputProgram.getFactory());
        testCase.process(clone);

        TestLoggingInstrumenter logging = new TestLoggingInstrumenter();
        logging.setLogger(logger + ".Logger");
        logging.setFactory(inputProgram.getFactory());
        logging.process(clone);

        return clone;
    }

    protected CtMethod cloneMethod(CtMethod method) {
        CtMethod cloned_method = method.getFactory().Core().clone(method);
//        cloned_method.setParent(method.getParent());

        CtAnnotation toRemove = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Override"))
                .findFirst().orElse(null);

        if(toRemove != null) {
            cloned_method.removeAnnotation(toRemove);
        }
        return cloned_method;
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
        Coverage coverage = new Coverage();

        for(TestCoverage tc : ampCoverage) {
            coverage.merge(tc.getCoverage());
        }

        return coverage;
    }
}
