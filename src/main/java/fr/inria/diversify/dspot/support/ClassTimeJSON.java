package fr.inria.diversify.dspot.support;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/17
 */
public class ClassTimeJSON {

    private final String fullQualifiedName;
    private final long timeInMs;

    public ClassTimeJSON(String fullQualifiedName, long timeInMs) {
        this.fullQualifiedName = fullQualifiedName;
        this.timeInMs = timeInMs;
    }
}
