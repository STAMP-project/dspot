package eu.stamp_project.dspot.selector.json.mutant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/17/17
 */
public class TestClassJSON {

    public final int nbMutantKilledOriginally;
    private final String name;
    private final long nbOriginalTestCases;
    private List<TestCaseJSON> testCases;

    public TestClassJSON(int nbMutantKilledOriginally, String name, long nbOriginalTestCases) {
        this.nbMutantKilledOriginally = nbMutantKilledOriginally;
        this.name = name;
        this.nbOriginalTestCases = nbOriginalTestCases;
        this.testCases = new ArrayList<>();
    }

    public boolean addTestCase(TestCaseJSON testCaseJSON) {
        if (this.testCases == null) {
            this.testCases = new ArrayList<>();
        }
        return this.testCases.add(testCaseJSON);
    }

}
