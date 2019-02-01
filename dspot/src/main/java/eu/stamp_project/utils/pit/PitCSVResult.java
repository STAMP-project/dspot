package eu.stamp_project.utils.pit;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class PitCSVResult extends AbstractPitResult{

    public PitCSVResult(String fullQualifiedNameOfMutatedClass, State stateOfMutant,
                        String fullQualifiedNameMutantOperator,
                        String fullQualifiedNameMethod, String fullQualifiedNameOfKiller,
                        int lineNumber,
                        String nameOfLocalisation) {
        super(fullQualifiedNameOfMutatedClass, stateOfMutant, fullQualifiedNameMutantOperator, fullQualifiedNameMethod,
                fullQualifiedNameOfKiller, lineNumber, nameOfLocalisation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PitCSVResult result = (PitCSVResult) o;

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
        return "PitCSVResult{" +
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

    @Override
    public PitCSVResult clone() {
        return new PitCSVResult(fullQualifiedNameOfMutatedClass, stateOfMutant, fullQualifiedNameMutantOperator,
                simpleNameMethod, fullQualifiedNameOfKiller, lineNumber, nameOfMutatedMethod);
    }
}
