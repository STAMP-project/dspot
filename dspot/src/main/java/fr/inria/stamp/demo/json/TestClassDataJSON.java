package fr.inria.stamp.demo.json;

import java.util.ArrayList;
import java.util.List;

public class TestClassDataJSON {

    public final String qualifiedName;
    public final long originalNbMutantCovered;
    public final long originalNbMutantKilled;
    public final long maxTests;
    public final long maxAssertions;
    public final List<DataJSON> data;

    public TestClassDataJSON(String qualifiedName, long originalNbMutantCovered, long originalNbMutantKilled, long maxTests, long maxAssertions) {
        this.qualifiedName = qualifiedName;
        this.originalNbMutantCovered = originalNbMutantCovered;
        this.originalNbMutantKilled = originalNbMutantKilled;
        this.maxTests = maxTests;
        this.maxAssertions = maxAssertions;
        this.data = new ArrayList<>();
    }
}
