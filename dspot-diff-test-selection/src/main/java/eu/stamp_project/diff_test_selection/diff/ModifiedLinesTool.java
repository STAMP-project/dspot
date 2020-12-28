package eu.stamp_project.diff_test_selection.diff;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import eu.stamp_project.diff_test_selection.selector.DiffTestSelectionImpl;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.util.*;

public class ModifiedLinesTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifiedLinesTool.class);

    private final String pathToFirstVersion;
    private final String pathToSecondVersion;

    private Map<String, List<Integer>> deletionPerQualifiedName;
    private Map<String, List<Integer>> additionPerQualifiedName;

    private Map<String, Set<String>> modifiedTestsPerTestClass;

    public ModifiedLinesTool(final String pathToFirstVersion, final String pathToSecondVersion) {
        this.pathToFirstVersion = pathToFirstVersion;
        this.pathToSecondVersion = pathToSecondVersion;
    }

    public Map<String, List<Integer>> getDeletionPerQualifiedName() {
        return deletionPerQualifiedName;
    }

    public Map<String, List<Integer>> getAdditionPerQualifiedName() {
        return additionPerQualifiedName;
    }

    public Map<String, Set<String>> getModifiedTestsPerTestClass() {
        return modifiedTestsPerTestClass;
    }

    public boolean hasResult() {
        return this.deletionPerQualifiedName != null && this.additionPerQualifiedName != null;
    }

    public boolean isTest() {
        return this.modifiedTestsPerTestClass != null && !this.modifiedTestsPerTestClass.isEmpty();
    }

    public void compute(String currentLine, String secondLine) {
        final File baseDir = new File(this.pathToFirstVersion);
        final String file1 = ModifiedLinesUtils.getCorrectPathFile(currentLine);
        final String file2 = ModifiedLinesUtils.getCorrectPathFile(secondLine);
        if (ModifiedLinesUtils.shouldSkip(this.pathToFirstVersion, file1, file2)) {
            LOGGER.warn("Could not match " + file1 + " and " + file2);
            return;
        }
        final File f1 = ModifiedLinesUtils.getCorrectFile(baseDir.getAbsolutePath(), file1);
        final File f2 = ModifiedLinesUtils.getCorrectFile(this.pathToSecondVersion, file2);
        try {
            LOGGER.info(f1.getAbsolutePath());
            LOGGER.info(f2.getAbsolutePath());
            final Diff diff = new AstComparator().compare(f1, f2);
            if (f1.getAbsolutePath().contains("src/test/java/")) {
                LOGGER.info("Build modified Tests {}", f2.getAbsolutePath());
                this.buildModifiedTests(diff);
            } else {
                this.buildMap(diff);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error when trying to compare " + f1 + " and " + f2);
        }
    }

    private void buildModifiedTests(Diff compare) {
        this.modifiedTestsPerTestClass = new HashMap<>();
        final List<Operation> allOperations = compare.getAllOperations();
        for (Operation operation : allOperations) {
            final CtElement node = ModifiedLinesUtils.filterOperation(operation);
            final CtMethod<?> testMethod;
            if (node != null && (testMethod = node.getParent(CtMethod.class) ) != null) {
                final String testClassQualifiedName = node.getPosition()
                        .getCompilationUnit()
                        .getMainType()
                        .getQualifiedName();
                if (!this.modifiedTestsPerTestClass.containsKey(testClassQualifiedName)) {
                    this.modifiedTestsPerTestClass.put(testClassQualifiedName, new HashSet<>());
                }
                this.modifiedTestsPerTestClass.get(testClassQualifiedName).add(testMethod.getSimpleName());
            }
        }
    }

    private void buildMap(Diff compare) {
        this.additionPerQualifiedName = new LinkedHashMap<>();// keeps the order
        this.deletionPerQualifiedName = new LinkedHashMap<>();// keeps the order
        final List<Operation> allOperations = compare.getAllOperations();
        final List<CtStatement> statements = new ArrayList<>();
        for (Operation operation : allOperations) {
            if (!isDeletionOrAddition(operation.getAction())) {
                continue;
            }
            final CtElement node = ModifiedLinesUtils.filterOperation(operation);
            if (node != null && !statements.contains(node.getParent(CtStatement.class))) {
                final int line = node.getPosition().getLine();
                final String qualifiedName = node
                        .getPosition()
                        .getCompilationUnit()
                        .getMainType()
                        .getQualifiedName();
                this.addToCorrespondingMap(operation.getAction(), qualifiedName, line);
                // TODO
//                if (!(node.getParent(CtStatement.class) instanceof CtBlock<?>)) {
//                    this.coverage.addModifiedLine(qualifiedName, line);
//                }
                statements.add(node.getParent(CtStatement.class));
            }
        }
    }

    private void addToCorrespondingMap(Action action, String qualifiedName, int line) {
        if (action instanceof Insert) {
            this.addToGivenMap(this.additionPerQualifiedName, qualifiedName, line);
        } else {
            this.addToGivenMap(this.deletionPerQualifiedName, qualifiedName, line);
        }
    }

    private void addToGivenMap(Map<String, List<Integer>> map, String qualifiedName, int line) {
        if (!map.containsKey(qualifiedName)) {
            map.put(qualifiedName, new ArrayList<>());
        }
        map.get(qualifiedName).add(line);
    }

    private boolean isDeletionOrAddition(Action action) {
        return action instanceof Insert || action instanceof Delete;
    }

}
