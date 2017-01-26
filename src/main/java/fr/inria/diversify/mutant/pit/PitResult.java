package fr.inria.diversify.mutant.pit;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class PitResult {

    public enum State {SURVIVED, KILLED, NO_COVERAGE, TIMED_OUT, NON_VIABLE, MEMORY_ERROR}

    private final State stateOfMutant;

    private final String fullQualifiedNameMutantOperator;

    private final int lineNumber;

    private final String location;

    private final String simpleNameMethod;

    public PitResult(State stateOfMutant, String fullQualifiedNameMutantOperator, String fullQualifiedNameMethod, int lineNumber, String nameOfLocalisation) {
        this.stateOfMutant = stateOfMutant;
        this.fullQualifiedNameMutantOperator = fullQualifiedNameMutantOperator;
        String[] split = fullQualifiedNameMethod.split("\\.");
        this.simpleNameMethod = split[split.length - 1];
        this.lineNumber = lineNumber;
        this.location = nameOfLocalisation;
    }

    public State getStateOfMutant() {
        return stateOfMutant;
    }

    public String getFullQualifiedNameMutantOperator() {
        return fullQualifiedNameMutantOperator;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLocation() {
        return location;
    }

    public CtMethod getMethod(CtType ctClass) {
        if ("none".equals(this.simpleNameMethod)) {
            return null;
        } else {
            String[] splittedQualifiedName = this.simpleNameMethod.split("\\.");
            String simpleNameOfMethod = splittedQualifiedName[splittedQualifiedName.length - 1];
            return (CtMethod) ctClass.getMethodsByName(simpleNameOfMethod).get(0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PitResult result = (PitResult) o;

        if (lineNumber != result.lineNumber) return false;
        if (!location.equals(result.location)) return false;
        return simpleNameMethod.equals(result.simpleNameMethod);

    }

    @Override
    public int hashCode() {
        int result = stateOfMutant != null ? stateOfMutant.hashCode() : 0;
        result = 31 * result + (fullQualifiedNameMutantOperator != null ? fullQualifiedNameMutantOperator.hashCode() : 0);
        result = 31 * result + lineNumber;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PitResult{" +
                "stateOfMutant=" + stateOfMutant +
                ", fullQualifiedNameMutantOperator='" + fullQualifiedNameMutantOperator + '\'' +
                ", lineNumber=" + lineNumber +
                ", location='" + location + '\'' +
                ", simpleNameMethod='" + simpleNameMethod + '\'' +
                '}';
    }
}
