package eu.stamp_project.diff_test_selection.utils;

public class DiffTestSelectionChecker {

    public static boolean checkIfDiffLineIsAJavaFileModification(final String currentLine) {
        return (currentLine.startsWith("+++") || currentLine.startsWith("---")) &&
                !DiffTestSelectionUtils.getJavaFile(currentLine).isEmpty();
    }

}
