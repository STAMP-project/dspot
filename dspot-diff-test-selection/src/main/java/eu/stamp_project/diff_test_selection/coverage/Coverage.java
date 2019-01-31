package eu.stamp_project.diff_test_selection.coverage;

import org.apache.maven.plugin.logging.Log;

import java.util.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/09/18
 * <p>
 * This class is responsible to compute the Coverage of the provided diff.
 */
public class Coverage {

    private Map<String, Set<Integer>> executedLinePerQualifiedName;

    private Map<String, Set<Integer>> modifiedLinePerQualifiedName;

    private final Log log;

    public Coverage(Log log) {
        this.modifiedLinePerQualifiedName = new LinkedHashMap<>();
        this.executedLinePerQualifiedName = new LinkedHashMap<>();
        this.log = log;
    }

    public void covered(String fullQualifiedName, Integer line) {
        if (!this.executedLinePerQualifiedName.containsKey(fullQualifiedName)) {
            this.executedLinePerQualifiedName.put(fullQualifiedName, new HashSet<>());
        }
        if (this.modifiedLinePerQualifiedName.containsKey(fullQualifiedName) &&
                this.modifiedLinePerQualifiedName.get(fullQualifiedName).contains(line)
                && this.executedLinePerQualifiedName.get(fullQualifiedName).add(line)) {
            this.log.info(fullQualifiedName + ":" + line + " covered.");
        }
    }

    public void addModifiedLine(String fullQualifiedName, Integer line) {
        if (!this.modifiedLinePerQualifiedName.containsKey(fullQualifiedName)) {
            this.modifiedLinePerQualifiedName.put(fullQualifiedName, new HashSet<>());
        }
        this.modifiedLinePerQualifiedName.get(fullQualifiedName).add(line);
        log.info(fullQualifiedName + ":" + line + " is modified.");
    }

    @Deprecated
    public void addModifiedLines(final Map<String, List<Integer>> newModifiedLinesPerQualifiedName) {
        newModifiedLinesPerQualifiedName.keySet()
                .forEach(key -> {
                            if (!this.modifiedLinePerQualifiedName.containsKey(key)) {
                                this.modifiedLinePerQualifiedName.put(key, new HashSet<>());
                            }
                            newModifiedLinesPerQualifiedName.get(key)
                                    .forEach(line -> {
                                                if (this.modifiedLinePerQualifiedName.get(key).add(line)) {
                                                    log.info(key + ":" + line + " is modified.");
                                                }
                                            }
                                    );
                        }
                );
    }

    public Map<String, Set<Integer>> getExecutedLinePerQualifiedName() {
        return executedLinePerQualifiedName;
    }

    public Map<String, Set<Integer>> getModifiedLinePerQualifiedName() {
        return modifiedLinePerQualifiedName;
    }
}
