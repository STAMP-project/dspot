package eu.stamp_project.utils.report.output.selector.change.json;

import java.util.ArrayList;
import java.util.List;
/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/09/18
 */
public class TestClassJSON implements eu.stamp_project.utils.report.output.selector.TestClassJSON {

    public final String name;
    public final long nbOriginalTestCases;
    public long nbTestAmplified;
    public final List<TestCaseJSON> testCases;


    public TestClassJSON(String name, long nbOriginalTestCases) {
        this.name = name;
        this.nbOriginalTestCases = nbOriginalTestCases;
        this.testCases = new ArrayList<>();
    }

    public TestClassJSON(String name, long nbOriginalTestCases, long nbTestAmplified, List<TestCaseJSON> testCases) {
        this.name = name;
        this.nbOriginalTestCases = nbOriginalTestCases;
        this.nbTestAmplified = nbTestAmplified;
        this.testCases = testCases;
    }

    //Not used
    public String toString(){
        return "";
    }
}
