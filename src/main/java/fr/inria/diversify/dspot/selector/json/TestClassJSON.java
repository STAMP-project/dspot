package fr.inria.diversify.dspot.selector.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/17/17
 */
public class TestClassJSON {

    private final String name;
    private List<TestCaseJSON> testCases;

    public TestClassJSON(String name) {
        this.name = name;
        this.testCases = new ArrayList<>();
    }

    public boolean addTestCase(TestCaseJSON testCaseJSON) {
        if (this.testCases == null) {
            this.testCases = new ArrayList<>();
        }
        return this.testCases.add(testCaseJSON);
    }

}
