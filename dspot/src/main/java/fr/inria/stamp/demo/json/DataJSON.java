package fr.inria.stamp.demo.json;

public class DataJSON {

    public final long nbMutantCovered;
    public final long nbMutantKilled;
    public final long nbTests;
    public final long nbAssertionsAdded;

    public DataJSON(long nbMutantCovered, long nbMutantKilled, long nbTests, long nbAssertionsAdded) {
        this.nbMutantCovered = nbMutantCovered;
        this.nbMutantKilled = nbMutantKilled;
        this.nbTests = nbTests;
        this.nbAssertionsAdded = nbAssertionsAdded;

    }
}
