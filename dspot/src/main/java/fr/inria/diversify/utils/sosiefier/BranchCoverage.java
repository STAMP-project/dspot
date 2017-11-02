package fr.inria.diversify.utils.sosiefier;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Simon
 * Date: 12/05/15
 * Time: 15:00
 */
@Deprecated
public class BranchCoverage {
    Set<Integer> deeps;
    String id;

    public BranchCoverage(String id, int deep) {
        this.id = id;
        deeps = new HashSet<>();
        deeps.add(deep);
    }

    public String getId() {
        return id;
    }

    protected void addDeep(int deep) {
        deeps.add(deep);
    }

    protected void addAllDeep(Set<Integer> deeps) {
        this.deeps.addAll(deeps);
    }

    public Set<Integer> getDeeps() {
        return deeps;
    }
}
