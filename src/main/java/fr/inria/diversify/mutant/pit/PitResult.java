package fr.inria.diversify.mutant.pit;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class PitResult {

    public enum State {SURVIVED, KILLED, NO_COVERAGE}

    private final State stateOfMutant;

    private final String fullQualifiedNameMutantOperator;

    private final String fullQualifiedNameTestMethod;

    private final int lineNumber;

    private final String location;

    public PitResult(State stateOfMutant, String fullQualifiedNameMutantOperator, String fullQualifiedNameTestMethod, int lineNumber, String nameOfLocalisation) {
        this.stateOfMutant = stateOfMutant;
        this.fullQualifiedNameMutantOperator = fullQualifiedNameMutantOperator;
        this.fullQualifiedNameTestMethod = fullQualifiedNameTestMethod;
        this.lineNumber = lineNumber;
        this.location = nameOfLocalisation;
    }

    public State getStateOfMutant() {
        return stateOfMutant;
    }

    public String getFullQualifiedNameMutantOperator() {
        return fullQualifiedNameMutantOperator;
    }

    public String getFullQualifiedNameTestMethod() {
        return fullQualifiedNameTestMethod;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLocation() {
        return location;
    }
}
