package eu.stamp_project.diff_test_selection;

import eu.stamp_project.diff_test_selection.diff.ModifiedLinesTool;
import eu.stamp_project.diff_test_selection.utils.DiffTestSelectionChecker;

import java.util.*;

public class EnhancedDiffTestSelection {

    private final String pathToFirstVersion;
    private final String pathToSecondVersion;
    private final Map<String, Map<String, Map<String, List<Integer>>>> coverageV1;
    private final Map<String, Map<String, Map<String, List<Integer>>>> coverageV2;
    private final String diff;

    public EnhancedDiffTestSelection(String pathToFirstVersion, String pathToSecondVersion,
                                     Map<String, Map<String, Map<String, List<Integer>>>> coverageV1,
                                     Map<String, Map<String, Map<String, List<Integer>>>> coverageV2,
                                     String diff) {
        this.pathToFirstVersion = pathToFirstVersion;
        this.pathToSecondVersion = pathToSecondVersion;
        this.coverageV1 = coverageV1;
        this.coverageV2 = coverageV2;
        this.diff = diff;
    }

    public Map<String, Set<String>> selectTests() {
        final Map<String, Set<String>> selectTestsPerTestClasses = new LinkedHashMap<>();
        final String[] lines = this.diff.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++) {
            final String currentLine = lines[i];
            if (DiffTestSelectionChecker.checkIfDiffLineIsAJavaFileModification(currentLine)) {
                final ModifiedLinesTool modifiedLinesTool = new ModifiedLinesTool(this.pathToFirstVersion, this.pathToSecondVersion);
                modifiedLinesTool.compute(currentLine, lines[++i]);
                // t1 that hits the deletions
                this.addTestsThatHitGivenChanges(
                        selectTestsPerTestClasses,
                        modifiedLinesTool.getDeletionPerQualifiedName(),
                        this.coverageV1
                );
                // t2 that hits the additions
                this.addTestsThatHitGivenChanges(
                        selectTestsPerTestClasses,
                        modifiedLinesTool.getAdditionPerQualifiedName(),
                        this.coverageV2
                );
            }
        }
        return selectTestsPerTestClasses;
    }

    private void addTestsThatHitGivenChanges(
            final Map<String, Set<String>> selectTestsPerTestClasses,
            Map<String, List<Integer>> modificationPerQualifiedName,
            Map<String, Map<String, Map<String, List<Integer>>>> coverage) {
        for (String modifiedClassFullQualifiedName : modificationPerQualifiedName.keySet()) {
            final List<Integer> modifiedLines = modificationPerQualifiedName.get(modifiedClassFullQualifiedName);
            for (String fullQualifiedNameOfTestClass : coverage.keySet()) {
                for (String testMethodName : coverage.get(fullQualifiedNameOfTestClass).keySet()) {
                    final Map<String, List<Integer>> coverageOfTestMethod = coverage.get(fullQualifiedNameOfTestClass).get(testMethodName);
                    if (coverageOfTestMethod.containsKey(modifiedClassFullQualifiedName)) {
                        if (modifiedLines.stream().anyMatch(line -> coverageOfTestMethod.get(modifiedClassFullQualifiedName).contains(line))) {
                            if (!selectTestsPerTestClasses.containsKey(fullQualifiedNameOfTestClass)) {
                                selectTestsPerTestClasses.put(fullQualifiedNameOfTestClass, new HashSet<>());
                            }
                            selectTestsPerTestClasses.get(fullQualifiedNameOfTestClass).add(testMethodName);
                        }
                    }

                }
            }
        }
    }
}
