package fr.inria.diversify.mutant.pit;

import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class PitResult {

    public enum State {SURVIVED, KILLED, NO_COVERAGE}

    private final State stateOfMutant;

    private final String fullQualifiedNameMutantOperator;

    private final CtMethod testCaseMethod;

    private final int lineNumber;

    private final String location;

    public PitResult(State stateOfMutant, String fullQualifiedNameMutantOperator, CtMethod testCaseMethod, int lineNumber, String nameOfLocalisation) {
        this.stateOfMutant = stateOfMutant;
        this.fullQualifiedNameMutantOperator = fullQualifiedNameMutantOperator;
        this.testCaseMethod = testCaseMethod;
        this.lineNumber = lineNumber;
        this.location = nameOfLocalisation;
    }

    public State getStateOfMutant() {
        return stateOfMutant;
    }

    public String getFullQualifiedNameMutantOperator() {
        return fullQualifiedNameMutantOperator;
    }

    public CtMethod getTestCaseMethod() {
        return testCaseMethod;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "PitResult{" +
                "stateOfMutant=" + stateOfMutant +
                ", fullQualifiedNameMutantOperator='" + fullQualifiedNameMutantOperator + '\'' +
                ", testCaseMethod='" + (testCaseMethod == null ? "none" : testCaseMethod.getSimpleName()) + '\'' +
                ", lineNumber=" + lineNumber +
                ", location='" + location + '\'' +
                '}';
    }
}
