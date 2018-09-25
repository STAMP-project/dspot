package eu.stamp_project.dspot.selector.json.change;

import java.util.ArrayList;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/09/18
 */
public class TestClassJSON {

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
}
