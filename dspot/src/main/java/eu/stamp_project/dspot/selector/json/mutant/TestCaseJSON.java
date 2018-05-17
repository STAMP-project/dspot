package eu.stamp_project.dspot.selector.json.mutant;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/17/17
 */
public class TestCaseJSON {

    private final String name;
    private final int nbAssertionAdded;
    private final int nbInputAdded;
    private final int nbMutantKilled;
    private final List<MutantJSON> mutantsKilled;

    public TestCaseJSON(String name, int nbAssertionAdded, int nbInputAdded, List<MutantJSON> mutantsKilled) {
        this.name = name;
        this.nbAssertionAdded = nbAssertionAdded;
        this.nbInputAdded = nbInputAdded;
        this.nbMutantKilled = mutantsKilled.size();
        this.mutantsKilled = mutantsKilled;
    }
}
