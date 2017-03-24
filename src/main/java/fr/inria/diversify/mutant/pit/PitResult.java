package fr.inria.diversify.mutant.pit;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class PitResult {

    public enum State {SURVIVED, KILLED, NO_COVERAGE, TIMED_OUT, NON_VIABLE, MEMORY_ERROR}

    private final String fullQualifiedNameClass;

    private final State stateOfMutant;

    private final String fullQualifiedNameMutantOperator;

    private final int lineNumber;

    private final String location;

    private final String simpleNameMethod;

    private CtMethod testCase = null;

    public PitResult(State stateOfMutant, String fullQualifiedNameMutantOperator, String fullQualifiedNameMethod, String fullQualifiedNameClass, int lineNumber, String nameOfLocalisation) {
        this.stateOfMutant = stateOfMutant;
        this.fullQualifiedNameMutantOperator = fullQualifiedNameMutantOperator;
        this.fullQualifiedNameClass = fullQualifiedNameClass;
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

    public String getFullQualifiedNameClass() {
        return fullQualifiedNameClass;
    }

    public CtMethod getMethod(CtType ctClass) {
        if ("none".equals(this.simpleNameMethod)) {
            return null;
        } else {
            if (this.testCase == null) {
                String[] splittedQualifiedName = this.simpleNameMethod.split("\\.");
                String simpleNameOfMethod = splittedQualifiedName[splittedQualifiedName.length - 1];
                List<CtMethod<?>> methodsByName = ctClass.getMethodsByName(simpleNameOfMethod);
                if (methodsByName.isEmpty()) {
                    if (ctClass.getSuperclass() != null) {
                        return getMethod(ctClass.getSuperclass().getDeclaration());
                    } else {
                        return null;
                    }
                }
                this.testCase = methodsByName.get(0);
            }
            return this.testCase;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PitResult result = (PitResult) o;

        if (lineNumber != result.lineNumber) return false;
        if (!location.equals(result.location)) return false;
        return fullQualifiedNameMutantOperator.equals(result.fullQualifiedNameMutantOperator);

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
