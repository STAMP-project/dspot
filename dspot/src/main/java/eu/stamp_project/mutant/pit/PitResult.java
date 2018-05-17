package eu.stamp_project.mutant.pit;

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

    private final String fullQualifiedNameOfMutatedClass;

    private final String fullQualifiedNameMutantOperator;

    private final String nameOfMutatedMethod;

    private final int lineNumber;

    private final State stateOfMutant;

    private final String fullQualifiedNameOfKiller;

    private final String simpleNameMethod;

    private CtMethod testCase = null;

    public PitResult(String fullQualifiedNameOfMutatedClass, State stateOfMutant,
                     String fullQualifiedNameMutantOperator,
                     String fullQualifiedNameMethod, String fullQualifiedNameOfKiller,
                     int lineNumber,
                     String nameOfLocalisation) {
        this.fullQualifiedNameOfMutatedClass = fullQualifiedNameOfMutatedClass;
        this.stateOfMutant = stateOfMutant;
        this.fullQualifiedNameMutantOperator = fullQualifiedNameMutantOperator;
        this.fullQualifiedNameOfKiller = fullQualifiedNameOfKiller;
        String[] split = fullQualifiedNameMethod.split("\\.");
        this.simpleNameMethod = split[split.length - 1];
        this.lineNumber = lineNumber;
        this.nameOfMutatedMethod = nameOfLocalisation;
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

    public String getNameOfMutatedMethod() {
        return nameOfMutatedMethod;
    }

    public String getFullQualifiedNameOfKiller() {
        return fullQualifiedNameOfKiller;
    }

    public CtMethod getMethod(CtType<?> ctClass) {
        if ("none".equals(this.simpleNameMethod)) {
            return null;
        } else {
            if (this.testCase == null) {
                List<CtMethod<?>> methodsByName = ctClass.getMethodsByName(this.simpleNameMethod);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PitResult result = (PitResult) o;

        return lineNumber == result.lineNumber &&
                fullQualifiedNameOfMutatedClass.endsWith(result.fullQualifiedNameOfMutatedClass) &&
                nameOfMutatedMethod.equals(result.nameOfMutatedMethod) &&
                fullQualifiedNameMutantOperator.equals(result.fullQualifiedNameMutantOperator);
    }

    @Override
    public int hashCode() {
        int result = stateOfMutant != null ? stateOfMutant.hashCode() : 0;
        result = 31 * result + (fullQualifiedNameMutantOperator != null ? fullQualifiedNameMutantOperator.hashCode() : 0);
        result = 31 * result + lineNumber;
        result = 31 * result + (nameOfMutatedMethod != null ? nameOfMutatedMethod.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PitResult{" +
                "fullQualifiedNameOfMutatedClass='" + fullQualifiedNameOfMutatedClass + '\'' +
                ", fullQualifiedNameMutantOperator='" + fullQualifiedNameMutantOperator + '\'' +
                ", nameOfMutatedMethod='" + nameOfMutatedMethod + '\'' +
                ", lineNumber=" + lineNumber +
                ", stateOfMutant=" + stateOfMutant +
                ", fullQualifiedNameOfKiller='" + fullQualifiedNameOfKiller + '\'' +
                ", simpleNameMethod='" + simpleNameMethod + '\'' +
                ", testCase=" + testCase +
                '}';
    }
}
