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

    public PitResult(State stateOfMutant, String fullQualifiedNameMutantOperator, String fullQualifiedNameTestMethod) {
        this.stateOfMutant = stateOfMutant;
        this.fullQualifiedNameMutantOperator = fullQualifiedNameMutantOperator;
        this.fullQualifiedNameTestMethod = fullQualifiedNameTestMethod;
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

    //generated
    @Override
    public String toString() {
        return "PitResult{" +
                "stateOfMutant=" + stateOfMutant +
                ", fullQualifiedNameMutantOperator='" + fullQualifiedNameMutantOperator + '\'' +
                ", fullQualifiedNameTestMethod='" + fullQualifiedNameTestMethod + '\'' +
                '}';
    }
}
