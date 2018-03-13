package fr.inria.stamp.demo.json;

import java.util.ArrayList;
import java.util.List;

public class TestClassDataJSON {

    public final String qualifiedName;
    public final long originalNbMutantCovered;
    public final long originalNbMutantKilled;
    public final List<DataJSON> data;

    public TestClassDataJSON(String qualifiedName, long originalNbMutantCovered, long originalNbMutantKilled) {
        this.qualifiedName = qualifiedName;
        this.originalNbMutantCovered = originalNbMutantCovered;
        this.originalNbMutantKilled = originalNbMutantKilled;
        this.data = new ArrayList<>();
    }
}
