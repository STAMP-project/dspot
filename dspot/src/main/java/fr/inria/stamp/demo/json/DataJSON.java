package fr.inria.stamp.demo.json;

public class DataJSON {

    public final int nbMutantKilled;
    public final int nbAssertionsAdded;
    public final int nbTests;

    public DataJSON(int nbMutantKilled, int nbAssertionsAdded, int nbTests) {
        this.nbMutantKilled = nbMutantKilled;
        this.nbAssertionsAdded = nbAssertionsAdded;
        this.nbTests = nbTests;
    }
}
