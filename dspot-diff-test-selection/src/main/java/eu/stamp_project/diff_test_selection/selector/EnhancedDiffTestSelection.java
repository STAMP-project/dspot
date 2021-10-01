package eu.stamp_project.diff_test_selection.selector;

import eu.stamp_project.diff_test_selection.coverage.ClassCoverage;
import eu.stamp_project.diff_test_selection.coverage.Coverage;
import eu.stamp_project.diff_test_selection.coverage.DiffCoverage;
import eu.stamp_project.diff_test_selection.diff.ModifiedLinesTool;
import eu.stamp_project.diff_test_selection.utils.DiffTestSelectionChecker;

import java.util.*;

public class EnhancedDiffTestSelection extends DiffTestSelection {

    private final Coverage coverageV2;

    public EnhancedDiffTestSelection(
            String pathToFirstVersion,
            String pathToSecondVersion,
            Coverage coverageV1,
            String diff,
            DiffCoverage coverage,
            Coverage coverageV2
    ) {
        super(pathToFirstVersion, pathToSecondVersion, coverageV1, diff, coverage);
        this.coverageV2 = coverageV2;
    }

    public Map<String, Set<String>> selectTests() {
        final Map<String, Set<String>> selectTestsPerTestClasses = new LinkedHashMap<>();
        final Map<String, Set<String>> modifiedTestsPerTestClass = new LinkedHashMap<>();
        final String[] lines = this.diff.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++) {
            final String currentLine = lines[i];
            if (DiffTestSelectionChecker.checkIfDiffLineIsAJavaFileModification(currentLine)) {
                final ModifiedLinesTool modifiedLinesTool = new ModifiedLinesTool(this.pathToFirstVersion, this.pathToSecondVersion);
                modifiedLinesTool.compute(currentLine, lines[++i]);
                if (modifiedLinesTool.hasResult()) {
                    // t1 that hits the deletions
                    this.addTestsThatHitGivenChanges(
                            selectTestsPerTestClasses,
                            modifiedLinesTool.getDeletionPerQualifiedName(),
                            this.coverageV1,
                            this.coverageV2
                    );
                    // t2 that hits the additions
                    this.addTestsThatHitGivenChanges(
                            selectTestsPerTestClasses,
                            modifiedLinesTool.getAdditionPerQualifiedName(),
                            this.coverageV2,
                            this.coverageV1
                    );
                } else if (modifiedLinesTool.isTest()) {
                    final Map<String, Set<String>> currentModifiedTestsPerTestClass = modifiedLinesTool.getModifiedTestsPerTestClass();
                    for (String testClassName : currentModifiedTestsPerTestClass.keySet()) {
                        if (!modifiedTestsPerTestClass.containsKey(testClassName)) {
                            modifiedTestsPerTestClass.put(testClassName, new HashSet<>());
                        }
                        modifiedTestsPerTestClass.get(testClassName).addAll(currentModifiedTestsPerTestClass.get(testClassName));
                    }
                }
            }
        }
        for (String testClassName : modifiedTestsPerTestClass.keySet()) {
            if (selectTestsPerTestClasses.containsKey(testClassName)) {
                selectTestsPerTestClasses.get(testClassName).removeAll(modifiedTestsPerTestClass.get(testClassName));
                if (selectTestsPerTestClasses.get(testClassName).isEmpty()) {
                    selectTestsPerTestClasses.remove(testClassName);
                }
            }
        }
        return selectTestsPerTestClasses;
    }

    private void addTestsThatHitGivenChanges(
            final Map<String, Set<String>> selectTestsPerTestClasses,
            Map<String, List<Integer>> modificationPerQualifiedName,
            Coverage coverage,
            Coverage otherCoverage
    ) {
        for (String modifiedClassFullQualifiedName : modificationPerQualifiedName.keySet()) {
            final List<Integer> modifiedLines = modificationPerQualifiedName.get(modifiedClassFullQualifiedName);
            this.coverage.addModifiedLines(modifiedClassFullQualifiedName, modifiedLines);
            for (String fullQualifiedNameOfTestClass : coverage.getTestClasses()) {
                for (String testMethodName : coverage.getTestMethodsForTestClassName(fullQualifiedNameOfTestClass)) {
                    final Map<String, ClassCoverage> testMethodCoverageForClassName = coverage.getTestMethodCoverageForClassName(fullQualifiedNameOfTestClass, testMethodName);
                    if (testMethodCoverageForClassName.containsKey(modifiedClassFullQualifiedName)) {
                        if (modifiedLines.stream().anyMatch(line -> testMethodCoverageForClassName.get(modifiedClassFullQualifiedName).contains(line))) {
                            if (otherCoverage.getTestClasses().contains(fullQualifiedNameOfTestClass) &&
                                    otherCoverage.getTestMethodsForTestClassName(fullQualifiedNameOfTestClass)
                                            .contains(testMethodName)
                            ) {
                                if (!selectTestsPerTestClasses.containsKey(fullQualifiedNameOfTestClass)) {
                                    selectTestsPerTestClasses.put(fullQualifiedNameOfTestClass, new HashSet<>());
                                }
                                modifiedLines.stream()
                                        .filter(line ->
                                                testMethodCoverageForClassName.get(modifiedClassFullQualifiedName).contains(line))
                                        .forEach(line -> this.coverage.covered(modifiedClassFullQualifiedName, line));
                                selectTestsPerTestClasses.get(fullQualifiedNameOfTestClass).add(testMethodName);
                            }
                        }
                    }
                }
            }
        }
    }
}
