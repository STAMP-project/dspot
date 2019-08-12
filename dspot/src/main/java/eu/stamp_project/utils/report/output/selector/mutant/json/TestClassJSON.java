package eu.stamp_project.utils.report.output.selector.mutant.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/17/17
 */
public class TestClassJSON implements eu.stamp_project.utils.report.output.selector.TestClassJSON {

    public final int nbMutantKilledOriginally;
    private final String name;
    private final long nbOriginalTestCases;
    private long nbNewMutantKilled;
    private List<TestCaseJSON> testCases;

    public TestClassJSON(int nbMutantKilledOriginally,String name, long nbOriginalTestCases) {
        this.nbMutantKilledOriginally = nbMutantKilledOriginally;
        this.name = name;
        this.nbOriginalTestCases = nbOriginalTestCases;
        this.testCases = new ArrayList<>();
        this.nbNewMutantKilled = 0;
    }

    public TestClassJSON(int nbMutantKilledOriginally,long nbNewMutantKilled,String name, long nbOriginalTestCases) {
        this(nbMutantKilledOriginally,name,nbOriginalTestCases);
        this.nbNewMutantKilled = nbNewMutantKilled;
    }

    public boolean addTestCase(TestCaseJSON testCaseJSON) {
        if (this.testCases == null) {
            this.testCases = new ArrayList<>();
        }
        return this.testCases.add(testCaseJSON);
    }

    public String toString(){
        String jsonString = new JSONObject()
                  .put("testName", name)
                  .put("originalKilledMutants", this.nbMutantKilledOriginally)
                  .put("NewMutantKilled", this.nbNewMutantKilled).toString();
        return jsonString;
    }

}
