package eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json;

import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ExtendedCoverage;

import java.util.ArrayList;
import java.util.List;

public class TestClassJSON implements eu.stamp_project.dspot.common.report.output.selector.TestClassJSON {


    private List<TestCaseJSON> testCases;
    private final ExtendedCoverage initialCoverage;
    private final CoverageImprovement amplifiedCoverage;
    private final ExtendedCoverage fullCoverageAfterAmplification;

    public TestClassJSON(ExtendedCoverage initialCoverage, CoverageImprovement amplifiedCoverage,
                         ExtendedCoverage fullCoverageAfterAmplification) {
        this.initialCoverage = initialCoverage;
        this.amplifiedCoverage = amplifiedCoverage;
        this.fullCoverageAfterAmplification = fullCoverageAfterAmplification;
    }

    public boolean addTestCase(TestCaseJSON testCaseJSON) {
        if (this.testCases == null) {
            this.testCases = new ArrayList<>();
        }
        return this.testCases.add(testCaseJSON);
    }

    public List<TestCaseJSON> getTestCases() {
        return this.testCases;
    }

    public ExtendedCoverage getInitialCoverage() {
        return initialCoverage;
    }

    public CoverageImprovement getAmplifiedCoverage() {
        return amplifiedCoverage;
    }

    public ExtendedCoverage getFullCoverageAfterAmplification() {
        return fullCoverageAfterAmplification;
    }
}
