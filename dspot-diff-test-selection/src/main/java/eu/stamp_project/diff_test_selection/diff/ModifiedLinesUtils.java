package eu.stamp_project.diff_test_selection.diff;

import eu.stamp_project.diff_test_selection.utils.DiffTestSelectionUtils;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class ModifiedLinesUtils {

    public static CtElement filterOperation(Operation operation) {
        if (operation.getSrcNode() != null &&
                filterOperationFromNode(operation.getSrcNode())) {
            return operation.getSrcNode();
        } else if (operation.getDstNode() != null &&
                filterOperationFromNode(operation.getDstNode())) {
            return operation.getDstNode();
        }
        return null;
    }

    public static boolean filterOperationFromNode(CtElement element) {
        try {
            return element.getPosition() != null &&
                    element.getPosition().getCompilationUnit() != null &&
                    element.getPosition().getCompilationUnit().getMainType() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean shouldSkip(String pathToFirstVersion, String file1, String file2) {
        if (file2.endsWith(file1)) {
            return false;
        }
        if (new File(file1).isAbsolute() && new File(file2).isAbsolute() &&
                file2.endsWith(file1.substring(pathToFirstVersion.length()))) {
            return false;
        }
        return true;
    }

    public static File getCorrectFile(String baseDir, String fileName) {
        File file = new File(fileName);
        if (file.isAbsolute() && file.exists()) {
            return file;
        }
        // TODO
//        if (fileName.substring(1).startsWith(this.configuration.module)) {
//            fileName = fileName.substring(this.configuration.module.length() + 1);
//        }
        file = new File(baseDir + "/" + fileName);
        return file.exists() ? file : new File(baseDir + "/../" + fileName);
    }

    public static String getCorrectPathFile(String path) {
        return removeDiffPrefix(DiffTestSelectionUtils.getJavaFile(path));
    }

    public static String removeDiffPrefix(String s) {
        return s.startsWith("a") || s.startsWith("b") ? s.substring(1) : s;
    }

}
