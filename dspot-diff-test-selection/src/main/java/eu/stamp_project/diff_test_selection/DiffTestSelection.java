package eu.stamp_project.diff_test_selection;

import eu.stamp_project.diff_test_selection.configuration.Configuration;
import eu.stamp_project.diff_test_selection.coverage.Coverage;
import eu.stamp_project.diff_test_selection.report.CSVReport;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/02/19
 */
public class DiffTestSelection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffTestSelection.class);

    private Map<String, Map<String, Map<String, List<Integer>>>> mapCoverage;

    private Coverage coverage;

    private Configuration configuration;

    public DiffTestSelection(Configuration configuration,
                             Map<String, Map<String, Map<String, List<Integer>>>> mapCoverage) {
        this.configuration = configuration;
        this.mapCoverage = mapCoverage;
        this.coverage = new Coverage();
    }

    public Coverage getCoverage() {
        return this.coverage;
    }

    Map<String, Set<String>> getTestThatExecuteChanges() {
        final Map<String, Set<String>> testMethodPerTestClasses = new LinkedHashMap<>();
        String[] lines = configuration.diff.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i];
            if ((currentLine.startsWith("+++") || currentLine.startsWith("---")) &&
                    !getJavaFile(currentLine).isEmpty()) {
                Map<String, List<Integer>> modifiedLinesPerQualifiedName =
                        getModifiedLinesPerQualifiedName(currentLine, lines[++i]);
                if (modifiedLinesPerQualifiedName == null) {
                    continue;
                }
                //this.coverage.addModifiedLines(modifiedLinesPerQualifiedName);
                Map<String, Set<String>> matchedChangedWithCoverage = matchChangedWithCoverage(this.mapCoverage, modifiedLinesPerQualifiedName);
                matchedChangedWithCoverage.keySet().forEach(key -> {
                    if (!testMethodPerTestClasses.containsKey(key)) {
                        testMethodPerTestClasses.put(key, matchedChangedWithCoverage.get(key));
                    } else {
                        testMethodPerTestClasses.get(key).addAll(matchedChangedWithCoverage.get(key));
                    }
                });
            }
        }
        return testMethodPerTestClasses;
    }

    private String getJavaFile(String currentLine) {
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

    private Map<String, Set<String>> matchChangedWithCoverage(Map<String, Map<String, Map<String, List<Integer>>>> coverage,
                                                              Map<String, List<Integer>> modifiedLinesPerQualifiedName) {
        Map<String, Set<String>> testClassNamePerTestMethodNamesThatCoverChanges = new LinkedHashMap<>();
        for (String testClassKey : coverage.keySet()) {
            for (String testMethodKey : coverage.get(testClassKey).keySet()) {
                if (this.isParametrized(testMethodKey)) {
                    continue;
                }
                for (String targetClassName : coverage.get(testClassKey).get(testMethodKey).keySet()) {
                    if (modifiedLinesPerQualifiedName.containsKey(targetClassName)) {
                        for (Integer line : modifiedLinesPerQualifiedName.get(targetClassName)) {
                            if (coverage.get(testClassKey).get(testMethodKey).get(targetClassName).contains(line)) {
                                // testClassKey#testMethodKey hits targetClassName#line
                                this.coverage.covered(targetClassName, line);
                                if (!testClassNamePerTestMethodNamesThatCoverChanges.containsKey(testClassKey)) {
                                    testClassNamePerTestMethodNamesThatCoverChanges.put(testClassKey, new HashSet<>());
                                }
                                testClassNamePerTestMethodNamesThatCoverChanges.get(testClassKey).add(testMethodKey);
                            }
                        }
                    }
                }
            }
        }
        return testClassNamePerTestMethodNamesThatCoverChanges;
    }

    private boolean isParametrized(String testMethodKey) {
        return testMethodKey.contains("[") && testMethodKey.contains("] ");
    }

    private Map<String, List<Integer>> getModifiedLinesPerQualifiedName(String currentLine,
                                                                        String secondLine) {
        final File baseDir = new File(this.configuration.pathToFirstVersion);
        final String file1 = getCorrectPathFile(currentLine);
        final String file2 = getCorrectPathFile(secondLine);
        if (shouldSkip(file1, file2)) {
            LOGGER.warn("Could not match " + file1 + " and " + file2);
            return null;
        }
        final File f1 = getCorrectFile(baseDir.getAbsolutePath(), file1);
        final File f2 = getCorrectFile(this.configuration.pathToSecondVersion, file2);
        try {
            LOGGER.info(f1.getAbsolutePath());
            LOGGER.info(f2.getAbsolutePath());
            return buildMap(new AstComparator().compare(f1, f2));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error when trying to compare " + f1 + " and " + f2);
            return null;
        }
    }

    private boolean shouldSkip(String file1, String file2) {
        if (file2.endsWith(file1)) {
            return false;
        }
        if (new File(file1).isAbsolute() && new File(file2).isAbsolute() &&
                file2.endsWith(file1.substring(this.configuration.pathToFirstVersion.length()))) {
            return false;
        }
        LOGGER.warn("Could not match " + file1 + " and " + file2);
        return true;
    }

    @NotNull
    private Map<String, List<Integer>> buildMap(Diff compare) {
        Map<String, List<Integer>> modifiedLinesPerQualifiedName = new LinkedHashMap<>();// keeps the order
        final List<Operation> allOperations = compare.getAllOperations();
        final List<CtStatement> statements = new ArrayList<>();
        for (Operation operation : allOperations) {
            final CtElement node = filterOperation(operation);
            if (node != null && !statements.contains(node.getParent(CtStatement.class))) {
                final int line = node.getPosition().getLine();
                final String qualifiedName = node
                        .getPosition()
                        .getCompilationUnit()
                        .getMainType()
                        .getQualifiedName();
                if (!modifiedLinesPerQualifiedName.containsKey(qualifiedName)) {
                    modifiedLinesPerQualifiedName.put(qualifiedName, new ArrayList<>());
                }
                modifiedLinesPerQualifiedName.get(qualifiedName).add(line);
                if (!(node.getParent(CtStatement.class) instanceof CtBlock<?>)) {
                    this.coverage.addModifiedLine(qualifiedName, line);
                }
                statements.add(node.getParent(CtStatement.class));
            }
        }
        return modifiedLinesPerQualifiedName;
    }

    /*
        UTILS METHODS
     */

    private CtElement filterOperation(Operation operation) {
        if (operation.getSrcNode() != null &&
                filterOperationFromNode(operation.getSrcNode())) {
            return operation.getSrcNode();
        } else if (operation.getDstNode() != null &&
                filterOperationFromNode(operation.getDstNode())) {
            return operation.getDstNode();
        }
        return null;
    }

    private boolean filterOperationFromNode(CtElement element) {
        return element.getPosition() != null &&
                element.getPosition().getCompilationUnit() != null &&
                element.getPosition().getCompilationUnit().getMainType() != null;
    }

    private File getCorrectFile(String baseDir, String fileName) {
        File file = new File(fileName);
        if (file.isAbsolute() && file.exists()) {
            return file;
        }
        if (fileName.substring(1).startsWith(this.configuration.module)) {
            fileName = fileName.substring(this.configuration.module.length() + 1);
        }
        file = new File(baseDir + "/" + fileName);
        return file.exists() ? file : new File(baseDir + "/../" + fileName);
    }

    private String getCorrectPathFile(String path) {
        return removeDiffPrefix(getJavaFile(path));
    }

    private String removeDiffPrefix(String s) {
        return s.startsWith("a") || s.startsWith("b") ? s.substring(1) : s;
    }

}
