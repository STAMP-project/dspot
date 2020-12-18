package eu.stamp_project.diff_test_selection.utils;

public class DiffTestSelectionUtils {

    public static String getJavaFile(String currentLine) {
        for (String item : currentLine.split(" ")) {
            if (item.endsWith(".java")) {
                return item;
            }
            for (String value : item.split("\t")) {
                if (value.endsWith(".java")) {
                    return value;
                }
            }
        }
        return "";
    }

}
