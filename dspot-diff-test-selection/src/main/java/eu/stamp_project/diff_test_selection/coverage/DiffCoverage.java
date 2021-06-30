package eu.stamp_project.diff_test_selection.coverage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/09/18
 * <p>
 * This class is responsible to compute the Coverage of the provided diff.
 */
public class DiffCoverage {

    private Map<String, Set<Integer>> executedLinePerQualifiedName;

    private Map<String, Set<Integer>> modifiedLinePerQualifiedName;

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCoverage.class);

    public DiffCoverage() {
        this.modifiedLinePerQualifiedName = new LinkedHashMap<>();
        this.executedLinePerQualifiedName = new LinkedHashMap<>();
    }

    public void covered(String fullQualifiedName, Integer line) {
        if (!this.executedLinePerQualifiedName.containsKey(fullQualifiedName)) {
            this.executedLinePerQualifiedName.put(fullQualifiedName, new HashSet<>());
        }
        if (this.modifiedLinePerQualifiedName.containsKey(fullQualifiedName) &&
                this.modifiedLinePerQualifiedName.get(fullQualifiedName).contains(line)
                && this.executedLinePerQualifiedName.get(fullQualifiedName).add(line)) {
            LOGGER.info(fullQualifiedName + ":" + line + " covered.");
        }
    }

    public void addModifiedLines(String fullQualifiedName, List<Integer> lines) {
        if (!this.modifiedLinePerQualifiedName.containsKey(fullQualifiedName)) {
            this.modifiedLinePerQualifiedName.put(fullQualifiedName, new HashSet<>());
        }
        lines.forEach(this.modifiedLinePerQualifiedName.get(fullQualifiedName)::add);
        LOGGER.info(fullQualifiedName + ":" + lines.toString() + " are modified.");
    }

    public void addModifiedLine(String fullQualifiedName, Integer line) {
        if (!this.modifiedLinePerQualifiedName.containsKey(fullQualifiedName)) {
            this.modifiedLinePerQualifiedName.put(fullQualifiedName, new HashSet<>());
        }
        this.modifiedLinePerQualifiedName.get(fullQualifiedName).add(line);
        LOGGER.info(fullQualifiedName + ":" + line + " is modified.");
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
                                                    LOGGER.info(key + ":" + line + " is modified.");
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
