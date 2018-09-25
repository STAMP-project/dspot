package eu.stamp_project.dspot.selector.json.change;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/09/18
 */
public class TestCaseJSON {

    public final String name;
    public final long nbInputAmplification;
    public final long nbAssertionAmplification;

    public TestCaseJSON(String name, long nbInputAmplification, long nbAssertionAmplification) {
        this.name = name;
        this.nbInputAmplification = nbInputAmplification;
        this.nbAssertionAmplification = nbAssertionAmplification;
    }
}
