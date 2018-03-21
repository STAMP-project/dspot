package fr.inria.stamp.demo.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/03/18
 */
public class TestClassDetailedDataJSON {

    public TestClassDetailedDataJSON(int nbAssertions, int nbTest, String qualifiedName) {
        this.nbAssertions = nbAssertions;
        this.nbTest = nbTest;
        this.qualifiedName = qualifiedName;
        this.mutants = new ArrayList<>();
    }

    public final int nbAssertions;
    public final int nbTest;
    public final String qualifiedName;
    public final List<MutantDataJSON> mutants;

}
